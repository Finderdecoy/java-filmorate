package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.service.FilmService;


class FilmServiceTest {
    private FilmService filmService;

    @BeforeEach
    public void beforeEach() {
        //this.filmService = new FilmService(new InMemoryFilmStorage(), new UserService(new InMemoryUserStorage()));
    }

    @Test
    public void testFilmLikedWhenRightParam() {
        //тесты в разработке. Нагоняю когорту
    }

    @Test
    public void testFilmLikedWhitWrongParam() {
        //тесты в разработке. Нагоняю когорту
    }
}
