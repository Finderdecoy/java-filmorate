package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component("memFilms")
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Long, Film> films = new HashMap<>();
    private static final LocalDate ORIGINAL_DATE_RELEASE = LocalDate.of(1895, 12, 28);
    private static final int CORRECT_LENGTH = 200;

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film addFilm(Film newFilm) {
        log.info("\n Фильм успешно добавлен -  {}", newFilm);
        long idFilm = nextId();
        newFilm.setId(idFilm);
        films.put(idFilm, newFilm);
        return newFilm;
    }

    @Override
    public Film editFilm(Film editFilm) {
        Long filmId = editFilm.getId();
        boolean isChanged = false;
        if (filmId == null) {
            throw new ValidationException("Неверно заполнены поля");
        }
        if (films.containsKey(filmId)) {
            Film oldFilm = films.get(filmId);
            if (editFilm.getDuration() != null && editFilm.getDuration().toMinutes() < 0) {
                oldFilm.setDuration(editFilm.getDuration());
                isChanged = true;
            }
            if (editFilm.getName() == null || editFilm.getName().isBlank()) {
                oldFilm.setName(editFilm.getName());
                isChanged = true;
            }
            if (editFilm.getDescription() != null && !editFilm.getDescription().isBlank()) {
                oldFilm.setDescription(editFilm.getDescription());
                isChanged = true;
            }
            if (editFilm.getReleaseDate() != null && !editFilm.getReleaseDate().isAfter(LocalDate.now()) &&
                    !editFilm.getReleaseDate().isBefore(ORIGINAL_DATE_RELEASE)) {
                oldFilm.setReleaseDate(editFilm.getReleaseDate());
                isChanged = true;
            }
            if (!isChanged) {
                logAllFields(editFilm);
            }
            films.put(filmId, oldFilm);
            return editFilm;
        }
        throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
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

    public Optional<Film> getByID(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}
