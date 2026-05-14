package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final HashMap<Long, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) {
        if (newFilm.getName().isBlank()) {
            throw new ValidationException("Название не должно быть пустым");
        }
        if (newFilm.getDescription().length() > 200) {
            throw new ValidationException("Длинна описания не должна превышать 200 символов");
        }
        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата фильма не должна быть младше 1895г. 28 числа декабря.");
        }
        if (newFilm.getDuration().toMinutes() < 0) {
            throw new ValidationException("Длина фильма должна быть положительным числом");
        }
        logAllFields(newFilm);
        log.info("\n Фильм успешно добавлен -  {}", newFilm);
        long idFilm = nextId();
        newFilm.setId(idFilm);
        films.put(idFilm, newFilm);
        return newFilm;
    }

    @PutMapping
    public Film editFilm(@RequestBody Film editFilm) {
        Long filmId = editFilm.getId();

        if (filmId == null) {
            throw new ValidationException("Неверно заполнены поля");
        }
        if (films.containsKey(filmId)) {
            if (editFilm.getDuration().toMinutes() < 0
                    || editFilm.getName().isBlank()
                    || editFilm.getDescription().isBlank()
                    || editFilm.getReleaseDate().isAfter(LocalDate.now())) {
                logAllFields(editFilm);
                throw new ValidationException("Неверно заполнены поля");
            }
            films.put(filmId, editFilm);
            return editFilm;
        }
        throw new ValidationException("Неверно заполнены поля");
    }

    private long nextId() {
        if (films.isEmpty()) {
            return 1L;
        }
        return Collections.max(films.keySet()) + 1;
    }

    private void logAllFields(Film film) {
        Long filmId = film.getId();
        String nameFilm = film.getName();
        String descriptionFilm = film.getDescription();
        LocalDate dateRelease = film.getReleaseDate();
        Duration dur = film.getDuration();

        log.info("Неверное заполнено поле \n id: {}\n Название:{} \n Описание: {} \n Дата выхода:{}\n Продолжительность:{}",
                filmId, nameFilm, descriptionFilm, dateRelease, dur);
    }
}
