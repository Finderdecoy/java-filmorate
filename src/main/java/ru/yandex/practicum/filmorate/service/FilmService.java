package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

@Slf4j
@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
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
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikeList().size()).reversed())
                .limit(count).toList();
    }

    private Film findFilmById(Long id) {
        return filmStorage.getFilms().stream()
                .filter(film -> Objects.equals(film.getId(), id))
                .findFirst().orElseThrow(() -> new NotFoundException("Фильм с id: " + id + " не найден."));
    }
}
