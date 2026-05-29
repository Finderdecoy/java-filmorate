package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private FilmStorage filmStorage;
    private FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film newFilm) {
        return filmStorage.addFilm(newFilm);
    }

    @PutMapping
    public Film editFilm(@RequestBody Film editFilm) {
        return filmStorage.editFilm(editFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void setLike(@PathVariable Long id,
                        @PathVariable Long userId) {
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
}
