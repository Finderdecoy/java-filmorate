package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {

    private FilmController filmController;

    @BeforeEach
    void beforeEach() {
        filmController = new FilmController();
    }

    @Test
    void testAddWhenFilmIsValid() {
        Film film = createValidFilm();

        Film createdFilm = filmController.addFilm(film);

        assertNotNull(createdFilm.getId(), "ID фильма не должен быть null");
        assertEquals(1L, createdFilm.getId(), "Первый ID должен быть равен 1");
        assertEquals(1, filmController.getFilms().size(), "В списке должен быть 1 фильм");
    }

    @Test
    void testPostWhitExceptionName() {
        Film film = createValidFilm();
        film.setName("   ");

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.addFilm(film)
        );
        assertEquals("Название не должно быть пустым", exception.getMessage());
    }

    @Test
    void testAddFilmWithSuccessAndExceptionLengthDescription() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.addFilm(film)
        );
        assertEquals("Длинна описания не должна превышать 200 символов", exception.getMessage());

        film.setDescription("a".repeat(200));

        assertEquals(film, filmController.addFilm(film), "Фильм должен добавится т.к. описание не превышает 200 символов");
    }

    @Test
    void testAddFilmSuccessAndExceptionDateRelease() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.addFilm(film)
        );
        assertEquals("Дата фильма не должна быть младше 1895г. 28 числа декабря.", exception.getMessage());

        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertEquals(film, filmController.addFilm(film), "Если фильм добавлен, должен вернутся добавленный фильм");
    }

    @Test
    void testAddDurationIsNegativeAndPositiveAndEmpty() {
        Film film = createValidFilm();
        film.setDuration(Duration.ofMinutes(-1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.addFilm(film)
        );
        assertEquals("Длина фильма должна быть положительным числом", exception.getMessage());

        film.setDuration(Duration.ofMinutes(1));
        assertEquals(film, filmController.addFilm(film), "Должно добавится т.к. Значение верное");
    }


    @Test
    void testPutWhenFilmExistsAndValid() {
        Film originalFilm = filmController.addFilm(createValidFilm());

        Film updatedData = new Film();
        updatedData.setId(originalFilm.getId());
        updatedData.setName("Обновленное название");
        updatedData.setDescription("Новое описание");
        updatedData.setReleaseDate(LocalDate.of(2020, 1, 1));
        updatedData.setDuration(Duration.ofMinutes(150));

        Film result = filmController.editFilm(updatedData);

        assertEquals("Обновленное название", result.getName());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void testPutWhenIdIsNull() {
        Film film = createValidFilm();
        film.setId(null);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.editFilm(film)
        );
        assertEquals("Неверно заполнены поля", exception.getMessage());
    }

    @Test
    void testPutWhenFilmDoesNotExist() {
        Film film = createValidFilm();
        film.setId(999L); // Несуществующий ID

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.editFilm(film)
        );
        assertEquals("Неверно заполнены поля", exception.getMessage());
    }


    @Test
    void testGetEmptyCollection() {
        Collection<Film> films = filmController.getFilms();
        assertTrue(films.isEmpty(), "Изначально список фильмов должен быть пуст");
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фильм о космических путешествиях Кристофера Нолана");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(Duration.ofMinutes(169));
        return film;
    }
}
