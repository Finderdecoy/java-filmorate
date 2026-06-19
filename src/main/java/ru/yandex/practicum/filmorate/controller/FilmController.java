package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate ORIGINAL_DATE_RELEASE = LocalDate.of(1895, 12, 28);
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film newFilm) {
        if (newFilm.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
            throw new ValidationException("Дата фильма не должна быть младше 1895г. 28 числа декабря.");
        }
        if (newFilm.getDuration().toMinutes() < 0) {
            throw new ValidationException("Длина фильма должна быть положительным числом");
        }
        return filmService.addFilm(newFilm);
    }

    @PutMapping
    public Film editFilm(@RequestBody Film editFilm) {
        log.info("Пришел фильм: {}", editFilm);
        if (editFilm.getId() == null) throw new ValidationException("Неверно заполнены поле ID");
        return filmService.editFilm(editFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void setLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        log.info("Получил film-id = {} - {} , получил user-id {} - {}", id, id.getClass(), userId, userId.getClass());
        filmService.setLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        filmService.delLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> mostPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.mostPopular(count);
    }

    @GetMapping("/{id}")
    public Film filmsWithGenre(@PathVariable Long id) {
        return filmService.getWithGenre(id);
    }

}
