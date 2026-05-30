package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;

public interface FilmStorage {
    public Collection<Film> getFilms();

    public Film addFilm(Film newFilm);

    public Film editFilm(Film editFilm);

    public HashMap<Long, Film> getFilmStorage();
}
