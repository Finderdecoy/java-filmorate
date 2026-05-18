package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final HashMap<Long, Film> films = new HashMap<>();
    private static final LocalDate ORIGINAL_DATE_RELEASE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film newFilm) { //что смог, пустил в валидатор
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
            throw new ValidationException("Дата фильма не должна быть младше 1895г. 28 числа декабря.");
        }
        //logAllFields(newFilm);
        log.info("\n Фильм успешно добавлен -  {}", newFilm);
        long idFilm = nextId();
        newFilm.setId(idFilm);
        films.put(idFilm, newFilm);
        return newFilm;
    }

    @PutMapping
    public Film editFilm(@RequestBody Film editFilm) {  // не стал делать валидацию иначе он выкинет ошибку и не изменит
        Long filmId = editFilm.getId();                 // нужные поля, которые валидные по значению
        boolean isChanged = false;
        if (filmId == null) {
            throw new ValidationException("Такого ID нет");
        }
        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            if (editFilm.getDuration() != null && editFilm.getDuration() < 0) {
                oldFilm.setDuration(editFilm.getDuration());
                isChanged = true;
            }
            if (editFilm.getName() != null && !editFilm.getName().isBlank()) {
                oldFilm.setName(editFilm.getName());
                isChanged = true;
            }
            if (!editFilm.getDescription().isBlank()) {
                oldFilm.setDescription(editFilm.getDescription());
                isChanged = true;
            }
            if (!editFilm.getReleaseDate().isAfter(LocalDate.now()) && !editFilm.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
                oldFilm.setReleaseDate(editFilm.getReleaseDate());
                isChanged = true;
            }
            if (!isChanged) {
                log.info("Фильм не был отредактирован");
                logAllFields(editFilm);
            }
            films.put(filmId, oldFilm);
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
        Integer dur = film.getDuration();

        log.info("Неверное заполнено поле \n id: {}\n Название:{} \n Описание: {} \n Дата выхода:{}\n Продолжительность:{}",
                filmId, nameFilm, descriptionFilm, dateRelease, dur);
    }
}
