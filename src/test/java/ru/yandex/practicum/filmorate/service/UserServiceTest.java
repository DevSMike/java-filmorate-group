package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
class UserServiceTest {
    final FilmService filmService;
    final UserService userService;

    @Autowired
    public UserServiceTest(UserService userService, FilmService filmService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    Film generateFilm() {
        Film film = new Film("Test Movie", "Test desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        return film;
    }

    User generateUser() {
        User user = new User("b@test.user", "testLogin", LocalDate.of(2020, 2, 2));
        user.setName("testName");
        return user;
    }

    @Test
    void getRecommendation() {
        Film filmWithId1 = filmService.addFilm(generateFilm());
        Film filmWithId2 = filmService.addFilm(generateFilm());
        Film filmWithId3 = filmService.addFilm(generateFilm());

        User userFromDb1 = userService.addUser(generateUser());
        User userFromDb2 = userService.addUser(generateUser());
        User userFromDb3 = userService.addUser(generateUser());

        filmService.addRate(userFromDb1.getId(), filmWithId2.getId(), 8);
        filmService.addRate(userFromDb1.getId(), filmWithId3.getId(), 7);
        filmService.addRate(userFromDb2.getId(), filmWithId1.getId(), 7);
        filmService.addRate(userFromDb3.getId(), filmWithId1.getId(),9);
        filmService.addRate(userFromDb3.getId(), filmWithId2.getId(), 7);
        filmService.addRate(userFromDb3.getId(), filmWithId3.getId(), 9);

        assertEquals(0, userService.getRecommendation(userFromDb3.getId()).size());
        assertEquals(1, userService.getRecommendation(userFromDb1.getId()).size());
        assertEquals(2, userService.getRecommendation(userFromDb2.getId()).size());
    }

    @Test
    void getRecommendationWithWrongId() {
        Throwable thrown = assertThrows(UserNotFoundException.class, () -> userService.getRecommendation(-1));
        assertEquals("UserID: " + -1, thrown.getMessage());
    }
}
