package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    public static final String QUERY_FOR_ALL_GENRE = "SELECT * FROM genre;";
    public static final String QUERY_BY_ID_GENRE = "SELECT * FROM genre WHERE id = ?";
    private final JdbcTemplate jdbc;

    public List<Genre> getAllGenre() {
        return jdbc.query(QUERY_FOR_ALL_GENRE, new GenreMapper());
    }

    public Genre getGenreById(Long id) {
        checkGenre(id);
        return jdbc.queryForObject(QUERY_BY_ID_GENRE, new GenreMapper(), id);
    }

    public void checkGenre(Long id) {
        Integer countRows = jdbc.queryForObject("SELECT COUNT(*) FROM GENRE WHERE id = ?;", Integer.class, id);
        if (countRows == null || countRows == 0) {
            throw new NotFoundException("Жанр с id : " + id + " не найден.");
        }
    }
}