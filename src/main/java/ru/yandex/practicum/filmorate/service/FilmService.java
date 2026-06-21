package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(@Qualifier("filmStorageDB") FilmStorage filmStorage,
                       UserService userService
    ) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public void setLike(Long id, Long userId) {
        userService.checkUsers(userId);
        filmStorage.setLike(id, userId);
    }

    public void delLike(Long id, Long userId) {
        Film film = findFilmById(id);
        User user = userService.findUserByID(userId);
        filmStorage.deleteLike(id, userId);
        log.info("Удален лайк у фильма: " + film.getName() + ". Пользователь: " + user.getName());
    }

    public Collection<Film> mostPopular(int count) {
        return filmStorage.mostPopular(count);
    }

    private Film findFilmById(Long id) {
        return filmStorage.getByID(id).orElseThrow(() -> new NotFoundException("Фильм с id: " + id + " не найден."));
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film editFilm(Film film) {
        return filmStorage.editFilm(film);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film getWithGenreByID(Long id) {
        Optional<Film> film = filmStorage.getByID(id);
        if (film.isEmpty()) throw new NotFoundException("Не найден фильм с id " + id);
        return film.get();
    }

}
