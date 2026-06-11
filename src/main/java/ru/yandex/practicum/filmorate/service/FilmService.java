package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service("dbFilms")
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private static final LocalDate ORIGINAL_DATE_RELEASE = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmStorageDB") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public void setLike(Long id, Long userId) {
        Film film = findFilmById(id);
        User user = userService.findUserByID(userId);
        film.getLikeList().add(user.getId());
        log.info("Фильму " + film.getName() + ". Поставлен лайк от пользователя: " + user.getName());
    }

    public void delLike(Long id, Long userId) {
        Film film = findFilmById(id);
        User user = userService.findUserByID(userId);
        film.getLikeList().remove(user.getId());
        log.info("Удален лайк у фильма: " + film.getName() + ". Пользователь: " + user.getName());
    }

    public Collection<Film> mostPopular(int count) {
        return filmStorage.getFilms().stream().sorted(Comparator.comparing((Film film) -> film.getLikeList().size()).reversed()).limit(count).toList();
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
        if (film.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
            throw new ValidationException("Дата фильма не должна быть младше 1895г. 28 числа декабря.");
        }
        if (film.getDuration().toMinutes() < 0) {
            throw new ValidationException("Длина фильма должна быть положительным числом");
        }
        return filmStorage.addFilm(film);
    }
}
