package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

//    @Autowired
//    public FilmController(FilmService filmService) {
//        this.filmService = filmService;
//    }

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
        return filmService.editFilm(editFilm);
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
