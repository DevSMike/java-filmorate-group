package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommonFilmsDbStorageTest {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private static long uid1;
    private static long uid2;
    private static long fid1;
    private static long fid2;
    private static long fid3;

    @Autowired
    public CommonFilmsDbStorageTest(
            @Qualifier("UserDbStorage")UserStorage userStorage,
            @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    private void fillFilmUserDb() {
        Film film1 = new Film("Film1 name", "First film description", LocalDate.now(), 100);
        film1.setMpa(new Mpa(1, ""));

        Film film2 = new Film("Film2 name", "Second film description", LocalDate.now(), 100);
        film2.setMpa(new Mpa(1, ""));

        Film film3 = new Film("Film3 title", "Third film description", LocalDate.now(), 100);
        film3.setMpa(new Mpa(1, ""));

        fid1 = filmStorage.addFilm(film1).getId();
        fid2 = filmStorage.addFilm(film2).getId();
        fid3 = filmStorage.addFilm(film3).getId();

        User user1 = new User("first@user.ru", "user1", LocalDate.now().minusYears(10));
        user1.setName("user1 name");

        User user2 = new User("second@main.ru", "user2", LocalDate.now().minusYears(10));
        user2.setName("user2 name");

        uid1 = userStorage.addUser(user1).getId();
        uid2 = userStorage.addUser(user2).getId();
    }

    @Test
    @Order(1)
    void getCommonFilmsWithoutLikesTest() {
        fillFilmUserDb();
        Collection<Film> common = filmStorage.getCommonFilms(uid1, uid2);
        assertEquals(0, common.size(), "У пользователей нет общих фильмов");
    }

    @Test
    @Order(2)
    void getCommonFilmsWithDifferentLikesTest() {
        Film film1 = filmStorage.getById(fid1);
        film1.getRates().put(uid1, 6);

        Film film2 = filmStorage.getById(fid2);

        film2.getRates().put(uid1, 8);
        filmStorage.updateFilm(film2);

        Collection<Film> common = filmStorage.getCommonFilms(uid1, uid2);
        assertEquals(0, common.size(), "У пользователей нет общих фильмов");
    }

    @Test
    @Order(3)
    void getCommonSingleFilmTest() {
        Film film = filmStorage.getById(fid3);
        film.getRates().put(uid1, 7);
        film.getRates().put(uid2, 8);
        filmStorage.updateFilm(film);

        Collection<Film> common = filmStorage.getCommonFilms(uid1, uid2);
        assertEquals(1, common.size(), "У пользователей должен быть один общий фильм");
        assertEquals(fid3, common.iterator().next().getId(), "Фильм должен иметь id = 3");
    }

    @Test
    @Order(4)
    void getSeveralCommonFilmsTest() {
        Film film = filmStorage.getById(fid2);
        film.getRates().put(uid1, 7);
        filmStorage.updateFilm(film);

        Film film2 = filmStorage.getById(fid2);
        film2.getRates().put(uid2, 8);
        filmStorage.updateFilm(film2);

        Collection<Film> common = filmStorage.getCommonFilms(uid1, uid2);
        assertEquals(2, common.size(), "У пользователей должен быть два общих фильма");
    }

    @Test
    @Order(5)
    void getCommonFilmsOneLikeRemovedTest() {
        Film film = filmStorage.getById(fid3);
        film.getRates().remove(uid1);
        filmStorage.updateFilm(film);
        Collection<Film> common = filmStorage.getCommonFilms(uid1, uid2);
        assertEquals(1, common.size(), "У пользователей должен быть один общий фильм");
        assertEquals(fid2, common.iterator().next().getId(), "Фильм должен иметь id = 2");
    }
}
