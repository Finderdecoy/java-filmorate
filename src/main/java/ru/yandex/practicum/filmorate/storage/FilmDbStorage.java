package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component("filmStorageDB")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final static String QUERY_FOR_ALL_FILMS = "SELECT * FROM films;";
    private final static String QUERY_FOR_GENRE_LIST = "SELECT * FROM film_genre;";
    private List<Genre> listGenre;

    @Override
    public Collection<Film> getFilms() {
        getListGenre();
        HashMap<Long, Set<Long>> mapGenreFilms = jdbc.query(QUERY_FOR_GENRE_LIST, rs -> {
            HashMap<Long, Set<Long>> filmsIdAndGenreId = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getInt("film_id");
                long genreId = rs.getInt("genre_id");
                filmsIdAndGenreId.computeIfAbsent(filmId, k ->
                        new HashSet<>()).add(genreId);
            }
            return filmsIdAndGenreId;
        });
        Collection<Film> films = jdbc.query(QUERY_FOR_ALL_FILMS, new FilmMapper());
        for (Film film : films) {
            Set<Long> genreIds = mapGenreFilms.getOrDefault(film.getId(), Set.of());
            Set<Genre> filmGenres = listGenre.stream()
                    .filter(genre -> genreIds.contains(genre.getId())
                    ).collect(Collectors.toSet());
            film.setGenre(filmGenres);
        }
        return films;
    }

    @Override
    public Film addFilm(Film newFilm) {
        jdbc.update("INSERT INTO films (name, description, release_date,duration, rates) VALUES (?, ?, ?, ?,?)",
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration().toMinutes(),
                1);
        return newFilm;
    }

    @Override
    public Film editFilm(Film editFilm) {
        return null;
    }

    @Override
    public Optional<Film> getByID(Long id) {
        return Optional.empty();
    }

    public List<Genre> getListGenre() {
        return listGenre = jdbc.query("SELECT * FROM GENRE;", new GenreMapper());
    }
}
