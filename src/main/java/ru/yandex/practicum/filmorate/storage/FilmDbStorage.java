package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;


@Slf4j
@Component("filmStorageDB")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    public static final String QUERY_INSERT_GENRE = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES(? , ?);";
    private static final LocalDate ORIGINAL_DATE_RELEASE = LocalDate.of(1895, 12, 28);
    private static final String QUERY_FOR_ALL_FILMS = "SELECT f.*, r.ID AS mpa_id, r.NAME_RATE AS mpa_name \n" +
            "FROM FILMS f \n" +
            "JOIN RATES r ON f.RATES = r.ID";
    private static final String QUERY_CREATE_FILM = "INSERT INTO films (name, description, release_date,duration, rates) VALUES (?, ?, ?, ?,?);";
    private static final String QUERY_FIND_BY_ID = QUERY_FOR_ALL_FILMS + "\nWHERE f.id = ?;";
    private static final String QUERY_UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rates = ? WHERE id = ? ; ";
    private final JdbcTemplate jdbc;

    @Override
    public Collection<Film> getFilms() {
        List<Film> films = jdbc.query(QUERY_FOR_ALL_FILMS, new FilmMapper());
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }


    @Override
    public Film addFilm(Film newFilm) {
        log.info("Жанры до операций над фильмом {}", newFilm.getGenres());
        checkMpaAndGenre(newFilm);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(QUERY_CREATE_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newFilm.getName());
            ps.setString(2, newFilm.getDescription());
            ps.setDate(3, Date.valueOf(newFilm.getReleaseDate()));
            ps.setInt(4, (int) newFilm.getDuration().toMinutes());
            ps.setLong(5, newFilm.getMpa().getId());
            return ps;
        }, keyHolder);
        newFilm.setId(keyHolder.getKey().longValue());
        log.info("После добаления фильма жанры стали вот такими : {}", newFilm.getGenres());
        setGenreForFilm(newFilm);
        log.info("Фильм добавлен - {}", newFilm);
        return newFilm;
    }


    @Override
    public Film editFilm(Film editFilm) {
        Long filmId = editFilm.getId();
        Optional<Film> films = getByID(filmId);
        if (films.isPresent()) {
            Film oldFilm = films.get();
            if (editFilm.getDuration() != null && editFilm.getDuration().toMinutes() > 0) {
                oldFilm.setDuration(editFilm.getDuration());
            }
            if (editFilm.getName() != null && !editFilm.getName().isEmpty()) {
                oldFilm.setName(editFilm.getName());
            }
            if (editFilm.getDescription() != null && !editFilm.getDescription().isBlank()) {
                oldFilm.setDescription(editFilm.getDescription());
            }
            if (editFilm.getReleaseDate() != null && !editFilm.getReleaseDate().isAfter(LocalDate.now())
                    && !editFilm.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
                oldFilm.setReleaseDate(editFilm.getReleaseDate());
            }
            if (editFilm.getMpa() != null) {
                oldFilm.setMpa(editFilm.getMpa());
            }
            setGenreForFilm(editFilm);
            jdbc.update(QUERY_UPDATE_FILM,
                    oldFilm.getName(),
                    oldFilm.getDescription(),
                    oldFilm.getReleaseDate(),
                    oldFilm.getDuration(),
                    oldFilm.getMpa().getId(),
                    oldFilm.getId());
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
    }

    private void setGenreForFilm(Film film) {
        Long filmId = film.getId();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> genreList = film.getGenres().stream().distinct().toList();
            jdbc.update("DELETE FROM FILM_GENRE WHERE FILM_ID= ? ;", filmId);
            jdbc.batchUpdate(QUERY_INSERT_GENRE,
                    new BatchPreparedStatementSetter() {

                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setLong(1, filmId);
                            ps.setLong(2, genreList.get(i).getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return genreList.size();
                        }
                    });
        }
    }

    @Override
    public Optional<Film> getByID(Long filmId) {
        Optional<Film> film = jdbc.query(QUERY_FIND_BY_ID, new FilmMapper(), filmId).stream().findFirst();
        Set<Genre> genres = new HashSet<>(jdbc.query("SELECT g.* FROM FILM_GENRE fg \n" +
                "INNER JOIN  genre AS g ON g.ID =fg.GENRE_ID \n" +
                "WHERE fg.FILM_ID = ?", new GenreMapper(), filmId));
        film.ifPresent(value -> value.setGenres(genres));
        return film;
    }

    @Override
    public Collection<Film> mostPopular(int count) {
        String sql = "SELECT f.*, r.ID AS mpa_id, r.NAME_RATE AS mpa_name " +
                "FROM FILMS f " +
                "JOIN RATES r ON f.RATES = r.ID " +
                "LEFT JOIN FILM_LIKES AS FL ON F.ID = FL.FILM_ID " +
                "GROUP BY F.ID, r.ID, r.NAME_RATE " +
                "ORDER BY COUNT(FL.USER_ID) DESC " +
                "LIMIT ?;";

        List<Film> popularFilms = jdbc.query(sql, new FilmMapper(), count);
        loadGenresForFilms(popularFilms);
        return popularFilms;
    }

    private void checkMpaAndGenre(Film newFilm) {
        Integer countMpa = jdbc.queryForObject("SELECT COUNT(*) FROM rates WHERE ID = ?",
                Integer.class, newFilm.getMpa().getId());
        if (countMpa == null || countMpa == 0)
            throw new NotFoundException("Не найден рейтинг с " + newFilm.getMpa().getId() + " id");

        if (newFilm.getGenres() != null) {
            if (newFilm.getGenres().isEmpty()) return;
            List<Long> genres = newFilm.getGenres().stream().map(genre -> genre.getId()).distinct().toList();
            String symbol = String.join(",", Collections.nCopies(genres.size(), "?"));
            Integer countGenre = jdbc.queryForObject("SELECT COUNT(*) FROM genre WHERE ID IN(" +
                    symbol + ")", Integer.class, genres.toArray());
            if (countGenre == null || countGenre != genres.size())
                throw new NotFoundException("Не найден один из жанров .");
        } else throw new NotFoundException("Жанры не найдены");
    }

    private void checkFilm(Long id) {
        String sqlChec = "SELECT COUNT(*) FROM FILMS WHERE id = ?;";
        Integer count = jdbc.queryForObject(sqlChec, Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с ID - " + id + " не был найден");
        }
    }

    @Override
    public void setLike(Long idFilm, Long idUser) {
        checkFilm(idFilm);
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, idFilm, idUser);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int delRows = jdbc.update(sql, id, userId);
        if (delRows == 0) {
            throw new NotFoundException("Лайк от пользователя с id " + userId + " не найден.");
        }
    }

    private void loadGenresForFilms(List<Film> films) {
        String sqlGenreMap = "SELECT f.id, g.ID AS genre_id ,g.NAME AS genre_name\n" +
                "FROM FILMS f\n" +
                "INNER JOIN FILM_GENRE fg ON fg.FILM_ID = f.ID \n" +
                "INNER JOIN\tGENRE g ON g.ID = fg.GENRE_ID \n";
        var mapGenre4Films = jdbc.query(sqlGenreMap, rs -> {
            HashMap<Long, Set<Genre>> map = new HashMap<>();
            while (rs.next()) {
                Genre genre = new Genre();
                genre.setId(rs.getLong("genre_id"));
                genre.setName(rs.getString("genre_name"));
                long filmId = rs.getLong("id");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return map;
        });
        for (Film film : films) {
            film.setGenres(mapGenre4Films.get(film.getId()));
        }
    }

    private void loadLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String sqlLikesMap = "SELECT FILM_ID, USER_ID FROM FILM_LIKES;";

        Map<Long, Set<Long>> mapLikes4Films = jdbc.query(sqlLikesMap, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("FILM_ID");
                long userId = rs.getLong("USER_ID");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return map;
        });

        for (Film film : films) {
            film.setLikeList(mapLikes4Films.getOrDefault(film.getId(), Collections.emptySet()));
        }
    }
}
