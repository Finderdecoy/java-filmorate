package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmServiceTest {

    private final FilmService filmService;

    @Test
    @Order(1)
    public void testFilmSetLike() {
        filmService.setLike(1L, 1L);
        var likeList = filmService.getFilms().stream().findFirst().get().getLikeList();
        assertThat(likeList).isNotEmpty().contains(1L);
    }

    @Test
    @Order(2)

    public void testFilmSetLikeWrongUser() {
        assertThatThrownBy(() -> filmService.setLike(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @Order(3)
    public void testSetLikeWrongFilm() {
        assertThatThrownBy(() ->
                filmService.setLike(2L, 1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @Order(4)
    public void testDeleteLike() {
        //Лайк стоит с предыдущего теста

        filmService.delLike(1L, 1L);
        var likeListAfter = filmService.getFilms().stream().findFirst().get().getLikeList();
        assertThat(likeListAfter).isEmpty();
    }

    @Test
    @Order(5)
    public void testDeleteLikeWrongUser() {
        assertThatThrownBy(() -> filmService.delLike(1L, 999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @Order(6)
    public void testDeleteLikeWrongFilm() {
        assertThatThrownBy(() -> filmService.delLike(999L, 1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @Order(7)
    public void testMostPopularList() {
        LocalDate releaseDate = LocalDate.of(1993, 1, 12);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("Комедия");

        Film film = new Film();
        film.setName("film1");
        film.setDuration(Duration.ofMinutes(45));
        film.setDescription("Description1");
        film.setMpa(mpa);
        film.setReleaseDate(LocalDate.of(1999, 1, 1));

        Film film1 = new Film();
        film1.setName("film2");
        film1.setDuration(Duration.ofMinutes(45));
        film1.setDescription("Description2");
        film1.setMpa(mpa);
        film1.setReleaseDate(releaseDate);

        Film film2 = new Film();
        film2.setName("film3");
        film2.setDuration(Duration.ofMinutes(45));
        film2.setDescription("Description3");
        film2.setReleaseDate(releaseDate);
        film2.setMpa(mpa);


        filmService.addFilm(film);
        filmService.addFilm(film1);
        filmService.addFilm(film2);


        filmService.setLike(2L, 1L);
        filmService.setLike(2L, 2L);
        filmService.setLike(2L, 3L);

        filmService.setLike(3L, 1L);
        filmService.setLike(3L, 2L);
        filmService.setLike(3L, 3L);
        filmService.setLike(3L, 4L);
        filmService.setLike(3L, 5L);

        List<Film> mostPopular = filmService.mostPopular(4).stream().peek(System.out::println).toList();
        assertThat(mostPopular).isNotEmpty().first().hasFieldOrPropertyWithValue("id", 3L);
        assertThat(mostPopular).last().hasFieldOrPropertyWithValue("id", 4L);
    }
}
