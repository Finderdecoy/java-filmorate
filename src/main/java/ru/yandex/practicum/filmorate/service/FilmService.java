package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final JdbcTemplate jdbc;

    public FilmService(@Qualifier("filmStorageDB") FilmStorage filmStorage,
                       UserService userService,
                       GenreService genreService,
                       JdbcTemplate jdbc) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreService = genreService;
        this.jdbc = jdbc;
    }

    public void setLike(Long id, Long userId) {
        checkFilm(id);
        userService.checkUsers(userId);
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, id, userId);
    }

    public void delLike(Long id, Long userId) {
        Film film = findFilmById(id);
        User user = userService.findUserByID(userId);
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int delRows = jdbc.update(sql, id, userId);
        if (delRows == 0) {
            throw new NotFoundException("Лайк от пользователя с id " + userId + " не найден.");
        }
        log.info("Удален лайк у фильма: " + film.getName() + ". Пользователь: " + user.getName());
    }

    public Collection<Film> mostPopular(int count) {
        return filmStorage.getFilms().stream().sorted(Comparator.comparing((Film film) -> film.getLikeList().size()).reversed()).limit(count).toList();
    }

    private Film findFilmById(Long id) {
        return filmStorage.getByID(id).orElseThrow(() -> new NotFoundException("Фильм с id: " + id + " не найден."));
    }

    public Collection<Film> getFilms() {


        return filmStorage.getFilms();
    }

    public Film editFilm(Film film) {
        return filmStorage.editFilm(film);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public void checkFilm(Long id) {
        String sqlChec = "SELECT COUNT(*) FROM FILMS WHERE id = ?;";
        Integer count = jdbc.queryForObject(sqlChec, Integer.class, id);
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с ID - " + id + " не был найден");
        }
    }

    public List<Film> getFilmsWithGenre(Long idGenre) {
        genreService.checkGenre(idGenre);
        /*List<Film> films = jdbc.query("SELECT * FROM FILMS AS F\n" +
                "INNER JOIN FILM_GENRE  AS FG  ON FG.FILM_ID = F.ID\n" +
                "INNER JOIN GENRE AS G ON G.ID = FG.GENRE_ID\n" +
                "WHERE G.ID = ? ", new FilmMapper(), idGenre);*/
        List<Film> films = filmStorage.getFilms().stream()
                .filter(film -> film.getGenres() != null &&
                        film.getGenres().stream().anyMatch(g -> g.getId() == idGenre))
                .toList();
        if(films.isEmpty()){
            throw new NotFoundException("Фильмы с таким жанром не найдены");
        }
        return films;
    }
}
