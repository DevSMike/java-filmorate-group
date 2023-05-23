package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final GenreStorage genreStorage;
    private final FilmService filmService;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Test
    void addFilm() {
        Film film = new Film("First Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        Film filmWithId = filmStorage.addFilm(film);
        assertEquals(film.getName(), filmWithId.getName());
        assertEquals("G", filmWithId.getMpa().getName());

        Film film2 = new Film("Second Movie", "Second desc", LocalDate.of(2020, 3, 2), 220);
        film2.setMpa(new Mpa(2, null));
        Film filmWithId2 = filmStorage.addFilm(film2);
        assertEquals(film2.getName(), filmWithId2.getName());
        assertEquals("PG", filmWithId2.getMpa().getName());
    }

    @Test
    void updateFilm() {
        Film filmZero = new Film("First Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        filmZero.setMpa(new Mpa(1, null));
        Film filmId = filmStorage.addFilm(filmZero);

        Film film = new Film("Updated Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        film.setId(filmId.getId());
        Film filmFromDb = filmStorage.updateFilm(film);
        assertEquals(filmId.getId(), filmFromDb.getId());
        assertEquals(film.getName(), filmFromDb.getName());
    }

    @Test
    void getAllFilms() {
        Film film = new Film("First Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        Film filmFromDb1 = filmStorage.addFilm(film);

        Film film2 = new Film("Second Movie", "Second desc", LocalDate.of(2020, 3, 2), 220);
        film2.setMpa(new Mpa(2, null));
        filmStorage.addFilm(film2);
        Collection<Film> films = filmStorage.getAllFilms();
        assertTrue(filmStorage.getAllFilms().stream().collect(Collectors.toMap(Film::getId, x -> x)).containsKey(filmFromDb1.getId()));
    }

    @Test
    void deleteFilm() {
        Film film = new Film("First Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        Film filmFromDb = filmStorage.addFilm(film);
        assertTrue(filmStorage.deleteFilm(filmStorage.getById(filmFromDb.getId())));
    }

    @Test
    void getById() {
        Film film = new Film("First Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        filmStorage.addFilm(film);

        Film film2 = new Film("Second Movie", "Second desc", LocalDate.of(2020, 3, 2), 220);
        film2.setMpa(new Mpa(2, null));
        Film filmFromDb = filmStorage.addFilm(film2);
        assertEquals("Second Movie", filmStorage.getById(filmFromDb.getId()).getName());
    }

    @Test
    void testGetByWithWrongId() {
        assertNull(filmStorage.getById(-3));
    }

    @Test
    void getTopFilms() {
        User user = new User("a@first.user", "login1", LocalDate.of(2020, 1, 1));
        user.setName("name1");
        User userFromDb1 = userStorage.addUser(user);
        User user2 = new User("b@second.user", "login2", LocalDate.of(2020, 2, 2));
        user2.setName("name2");
        User userFromDb2 = userStorage.addUser(user2);
        long user1Id = userFromDb1.getId();
        long user2Id = userFromDb2.getId();

        Film film = new Film("First Movie", "First desc", LocalDate.of(2019, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        Film film2 = new Film("Second Movie", "Second desc", LocalDate.of(2020, 3, 2), 220);
        film2.setMpa(new Mpa(2, null));
        Set<Genre> genresFilm1 = new HashSet<>();
        genresFilm1.add(genreStorage.getById(1).get());
        genresFilm1.add(genreStorage.getById(2).get());
        Set<Genre> genresFilm2 = new HashSet<>();
        genresFilm2.add(genreStorage.getById(1).get());

        film.setGenres(genresFilm1);
        film2.setGenres(genresFilm2);
        Film filmFromDb1 = filmStorage.addFilm(film);
        Film filmFromDb2 = filmStorage.addFilm(film2);
        long film1Id = filmFromDb1.getId();
        long film2Id = filmFromDb2.getId();

        filmService.addRate(user1Id, film2Id, 6);
        filmService.addRate(user2Id, film2Id, 4);
        filmService.addRate(user1Id, film1Id, 8);

        assertEquals(filmService.getTopFilms(2, genreStorage.getById(1).get().getId(), 0).size(), 2, "sizes are diff");
        assertEquals(new ArrayList<>(filmService.getTopFilms(2, genreStorage.getById(2).get().getId(), 0))
                .get(0).getName(), "First Movie", "names are diff");
        assertEquals(new ArrayList<>(filmService.getTopFilms(2, 0, 2019))
                .get(0).getName(), "First Movie", "names are diff");
    }
}