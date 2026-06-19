package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film addFilm(Film newFilm);

    Film editFilm(Film editFilm);

    Optional<Film> getByID(Long id);

    Collection<Film> mostPopular(int count);

    void setLike(Long idFilm, Long idUser);

    void deleteLike(Long id, Long userId);
}
