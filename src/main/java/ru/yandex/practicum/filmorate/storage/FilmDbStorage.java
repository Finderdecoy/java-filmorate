package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.mapper.RateMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component("filmStorageDB")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    private static final LocalDate ORIGINAL_DATE_RELEASE = LocalDate.of(1895, 12, 28);

    private final static String QUERY_FOR_ALL_FILMS = "SELECT * FROM films;";
    private final static String QUERY_FOR_GENRE_LIST = "SELECT * FROM film_genre;";
    private final static String QUERY_CREATE_FILM = "INSERT INTO films (name, description, release_date,duration, rates) VALUES (?, ?, ?, ?,?);";
    private final static String QUERY_SET_GENRE = "INSERT INTO film_genre (film_id,genre_id) VALUES (?,?);";
    private final static String QUERY_FIND_BY_ID = "SELECT * FROM films WHERE id = ?;";
    private static final String QUERY_UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rates = ? WHERE id = ? ; ";

    private List<Genre> listGenre ;
    private List<Mpa> mpaList;
    Map<Long,Set<Long>> listLikes;


    @Override
    public Collection<Film> getFilms() {
        getListGenre();
        HashMap<Long, Set<Long>> mapGenreFilms = jdbc.query(QUERY_FOR_GENRE_LIST, rs -> {
            HashMap<Long, Set<Long>> filmsIdAndGenreId = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getInt("film_id");
                long genreId = rs.getInt("genre_id");
                filmsIdAndGenreId.computeIfAbsent(filmId, k -> new HashSet<>()).add(genreId);
            }
            return filmsIdAndGenreId;
        });
        Collection<Film> films = jdbc.query(QUERY_FOR_ALL_FILMS, new FilmMapper());
        for (Film film : films) {
            Set<Long> genreIds = mapGenreFilms.getOrDefault(film.getId(), Set.of());
            Set<Genre> filmGenres = listGenre.stream()
                    .filter(genre -> genreIds.contains(genre.getId()))
                    .collect(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparingLong(Genre::getId))
                    ));
            film.setGenres(filmGenres);
        }
        return films;
    }

    @Override
    public Film addFilm(Film newFilm) {
        getListGenre();
        getListMPA();
        if (newFilm.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
            throw new ValidationException("Дата фильма не должна быть младше 1895г. 28 числа декабря.");
        }
        if (newFilm.getDuration().toMinutes() < 0) {
            throw new ValidationException("Длина фильма должна быть положительным числом");
        }
        if (newFilm.getGenres() == null || newFilm.getGenres().isEmpty()) {
            throw new NotFoundException("Жанра с таким ID не сушествует");
        }
        Set<Genre> genresFilm = newFilm.getGenres().stream().map(genre -> {
            return listGenre.stream().filter(gn -> genre.getId() == gn.getId())
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Жанра с таким ID не сушествует"));
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        Mpa mpa = mpaList.stream()
                .filter(rate -> rate.getId() == newFilm.getMpa().getId())
                .findFirst()
                .orElseThrow(()-> new NotFoundException("Рейтинг с таким ID  не найден"));
        newFilm.setGenres(genresFilm);
        newFilm.setMpa(mpa);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(QUERY_CREATE_FILM, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,newFilm.getName());
                ps.setString(2,newFilm.getDescription());
                ps.setDate(3, Date.valueOf(newFilm.getReleaseDate()));
                ps.setInt(4,(int) newFilm.getDuration().toMinutes());
                ps.setLong(5,newFilm.getMpa().getId());
                return ps;},keyHolder);
        for (Genre genre : newFilm.getGenres()) {
            jdbc.update(QUERY_SET_GENRE, keyHolder.getKey().longValue(), genre.getId());
        }
        newFilm.setId(keyHolder.getKey().longValue());
        return newFilm;
    }

    @Override
    public Film editFilm(Film editFilm) {
        getListGenre();
        Long filmId = editFilm.getId();
        if (filmId == null) {
            throw new ValidationException("Неверно заполнены поле ID");
        }
        Optional<Film> films = getByID(filmId);
        if (films.isPresent()) {
            Film oldFilm = films.get();
            if (editFilm.getDuration() != null && editFilm.getDuration().toMinutes() > 0) {
                oldFilm.setDuration(editFilm.getDuration());
            }
            if (editFilm.getName() != null || !editFilm.getName().isBlank()) {
                oldFilm.setName(editFilm.getName());
            }
            if (editFilm.getDescription() != null && !editFilm.getDescription().isBlank()) {
                oldFilm.setDescription(editFilm.getDescription());
            }
            if (editFilm.getReleaseDate() != null && !editFilm.getReleaseDate().isAfter(LocalDate.now())
                    && !editFilm.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
                oldFilm.setReleaseDate(editFilm.getReleaseDate());
            }
            if (!editFilm.getGenres().isEmpty()) {
                oldFilm.setGenres(editFilm.getGenres());
                for (Genre genre : oldFilm.getGenres()) {
                    jdbc.update(QUERY_SET_GENRE, filmId, genre.getName());
                }
            }
            if (editFilm.getMpa() != null) {
                oldFilm.setMpa(editFilm.getMpa());
            }
            log.info("Дошел до добавления фильма. Сам фильм : {}" , oldFilm);
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

    @Override
    public Optional<Film> getByID(Long filmId) {
        Optional<Film> films = jdbc.query(QUERY_FIND_BY_ID, new FilmMapper(), filmId).stream().findFirst();
        likedFilmsMap();
        films.get().setLikeList(listLikes.get(filmId));
        return films;
    }

    private void likedFilmsMap() {
        listLikes = jdbc.query("SELECT * FROM FILM_LIKES;",rs -> {
            Map<Long, Set<Long>> filmsIdAndUserLikes = new HashMap<>();
            while (rs.next()) {
                long filmID = rs.getLong("film_id");
                long userId = rs.getLong("user_id");
                filmsIdAndUserLikes.computeIfAbsent(filmID, k -> new HashSet<>()).add(userId);
            }
            return filmsIdAndUserLikes;
        });
    }

    private List<Genre> getListGenre() {
        return listGenre = jdbc.query("SELECT * FROM genre;", new GenreMapper());
    }

    private List<Mpa> getListMPA() {
        return mpaList = jdbc.query("SELECT * FROM rates;", new RateMapper());
    }
}
