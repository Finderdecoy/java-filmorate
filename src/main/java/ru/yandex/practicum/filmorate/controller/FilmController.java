package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private final GenreService genreService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film newFilm) {
        return filmService.addFilm(newFilm);
    }

    @PutMapping
    public Film editFilm(@RequestBody Film editFilm) {
        log.info("Пришел фильм: {}", editFilm);
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
        log.info("Дошел до сервиса");
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
