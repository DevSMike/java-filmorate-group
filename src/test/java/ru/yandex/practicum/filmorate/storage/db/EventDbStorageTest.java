package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperations;
import ru.yandex.practicum.filmorate.model.event.EventTypes;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventDbStorageTest {

    final EventStorage eventStorage;
    final UserService userService;
    final FilmService filmService;

    long userId;
    long user2Id;
    long filmId;

    @BeforeEach
    void beforeEach() {
        User user = new User("test@mail.ru", "test", LocalDate.of(2002, 12, 12));
        user.setName("name");
        User user2 = new User("test2@mail.ru", "test2", LocalDate.of(2003, 12, 12));
        user2.setName("name2");
        User userFromDb1 = userService.addUser(user);
        User userFromDb2 = userService.addUser(user2);
        userId = userFromDb1.getId();
        user2Id = userFromDb2.getId();
        Film film = new Film("First Movie", "First desc", LocalDate.of(2020, 3, 1), 120);
        film.setMpa(new Mpa(1, null));
        Film filmFromDb1 = filmService.addFilm(film);
        filmId = filmFromDb1.getId();
    }

    @Test
    void addEventCorrect() {
        userService.addFriend(userId, user2Id);
        Event event = Event.builder()
                .userId(userId)
                .entityId(user2Id)
                .eventType(EventTypes.FRIEND)
                .operation(EventOperations.ADD)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        assertEquals(event.getEventType(), eventStorage.getEventById(1).getEventType(), "ids are diff");
    }

    @Test
    void getUserEventsCorrect() {
        userService.addFriend(userId, user2Id);
        filmService.addRate(userId, filmId, 6);
        System.out.println(eventStorage.getUserEvents(userId));
        assertEquals(eventStorage.getUserEvents(userId).size(), 2, "sizes are diff");
        assertEquals((new ArrayList<>(eventStorage.getUserEvents(userId)))
                .get(0).getEventType().toString(), "FRIEND", "types are diff");
        assertEquals(eventStorage.getUserEvents(user2Id).size(), 0, "sizes are diff");
    }

    @Test
    void getEventById() {
        userService.addFriend(userId, user2Id);
        assertEquals(eventStorage.getEventById(1).getEventType().toString(), "FRIEND", "types are diff");
    }
}