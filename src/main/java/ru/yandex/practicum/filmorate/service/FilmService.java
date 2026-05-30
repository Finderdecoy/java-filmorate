package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

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
        if (getFilmsStorage().containsKey(id)) {
            return getFilmsStorage().get(id);
        }
        throw new NotFoundException("Фильм с id: " + id + " не найден.");
    }

    public HashMap<Long, Film> getFilmsStorage() {
        return filmStorage.getFilmStorage();
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
}
