package ru.yandex.practicum.filmorate.controllers.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;
    private final EventService eventService;


    public UserController(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @PostMapping()
    public User addUser(@Valid @RequestBody User user) {
        log.info("Post request for user");
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Put request for user");
        return userService.updateUser(user);
    }

    @GetMapping("/all")
    public Collection<User> getAllUsers() {
        log.info("Get request for users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable long id) {
        log.info("Get request for user: {}", id);
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Put request to add friend id: {} friendId: {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Delete request to remove friend id: {} friendId: {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriendsList(@PathVariable long id) {
        log.info("Get request for all friends list for user: {}", id);
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriendsList(@PathVariable Integer id, @PathVariable Integer otherId) {
        log.info("Get request for common friends userId: {} friendId: {}", id, otherId);
        return userService.getCommonFriendsList(id, otherId);
    }

    @DeleteMapping("/user/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.info("Delete request to remove user with id {}", userId);
        userService.deleteUser(userId);
    }

    @GetMapping("/{id}/feed")
    public Collection<Event> getUserEvents(@PathVariable Long id) {
        log.info("Get request for userId {} events", id);
        return eventService.getUserEvents(id);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable long id) {
        log.info("Get request for recommendation for user with id: {} ", id);
        return userService.getRecommendation(id);
    }
}
