package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})
class FilmorateDBStorageTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    //UserDbStorage
    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.getByID(1L);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testNonExistUser() {
        Optional<User> user = userStorage.getByID(2L);
        assertThat(user.isEmpty());
    }

    @Test
    public void testGetAllUsers() {
        Collection<User> listUsers = userStorage.getUsers();
        assertThat(listUsers).isNotNull();
    }

    @Test
    public void testAddUser() {
        User user = new User();
        user.setEmail("Loginov@yandex.ru");
        user.setLogin("Loginov");
        user.setName("Imya");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User userAdded = userStorage.addUser(user);
        assertThat(userAdded).isNotNull().hasFieldOrProperty("id");

    }

    @Test
    public void testAutoFieldName() {
        User user = new User();
        user.setEmail("Loginov@yandex.ru");
        user.setLogin("Loginov");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User userAdded = userStorage.addUser(user);

        assertThat(userAdded).isNotNull().hasFieldOrPropertyWithValue("name", "Loginov");
    }

    @Test
    public void testEditUser() {
        User user = userStorage.getByID(1L).get();
        user.setName("Edit Testovich");
        var editUser = userStorage.editingUser(user);
        assertThat(editUser).hasFieldOrPropertyWithValue("name", "Edit Testovich");
    }

    //FilmDBStorage
    @Test
    public void testGetAllFilms() {
        Collection<Film> films = filmStorage.getFilms();
        assertThat(films).isNotNull().hasSize(1)
                .extracting(film ->
                        assertThat(film).isNotNull().hasFieldOrPropertyWithValue("id", 1L));
    }


    @Test
    public void testAddFilm() {
        Film film = new Film();
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setName("TestFIlm");
        film.setDescription("Lorem Test Funfovich");
        film.setReleaseDate(LocalDate.of(1999, 1, 1));
        film.setDuration(Duration.ofMinutes(49));
        film.setMpa(mpa);

        Film filmAdded = filmStorage.addFilm(film);
        assertThat(filmAdded).hasFieldOrPropertyWithValue("id", 2L);
    }

    @Test
    public void testAddFilmWithOUTMPA() {
        Film film = new Film();
        film.setName("TestFIlm");
        film.setDescription("Lorem Test Funfovich");
        film.setReleaseDate(LocalDate.of(1999, 1, 1));
        film.setDuration(Duration.ofMinutes(49));

        assertThatThrownBy(() -> filmStorage.addFilm(film));
    }

    @Test
    public void testFilmGetByID() {
        var film = filmStorage.getByID(1L);

        assertThat(film).isPresent()
                .hasValueSatisfying(findingFilm ->
                        assertThat(findingFilm).hasFieldOrPropertyWithValue("id", 1L));
    }

    @Test
    public void testFimEdit() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("Комедия");

        Film editingFilm = new Film();
        editingFilm.setId(1L);
        editingFilm.setName("Now Test");
        editingFilm.setDescription("Now Test Description");
        editingFilm.setDuration(Duration.ofMinutes(1));
        editingFilm.setReleaseDate(LocalDate.of(2020,01,03));
        editingFilm.setMpa(mpa);

        Film result = filmStorage.editFilm(editingFilm);
        assertThat(result).hasFieldOrPropertyWithValue("name","Now Test");
    }
}
