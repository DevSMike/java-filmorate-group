package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperations;
import ru.yandex.practicum.filmorate.model.event.EventTypes;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final EventService eventService;

    public UserService(@Qualifier("UserDbStorage")UserStorage userStorage, EventService eventService) {
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    public User addUser(User user) {
        log.debug("Adding user: " + user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        log.debug("Updating user: " + user);
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        log.debug("Getting all users");
        return userStorage.getAllUsers();
    }

    public void addFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.debug("Adding friend: {} to user: {}", friend, user);
        user.getFriends().put(friendId, "Requested");
        userStorage.updateUser(user);
        eventService.addEvent(Event.builder()
                .userId(userId)
                .entityId(friendId)
                .eventType(EventTypes.FRIEND)
                .operation(EventOperations.ADD)
                .eventType(EventTypes.FRIEND)
                .operation(EventOperations.ADD)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    public void removeFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.debug("Removing friend: {} from user: {}", friend, user);
        user.getFriends().remove(friendId);
        userStorage.updateUser(user);
        eventService.addEvent(Event.builder()
                .userId(userId)
                .entityId(friendId)
                .eventType(EventTypes.FRIEND)
                .operation(EventOperations.REMOVE)
                .eventType(EventTypes.FRIEND)
                .operation(EventOperations.REMOVE)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    public Collection<User> getCommonFriendsList(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.debug("Get common friend for friend: {} and user: {}", friend, user);
        return userStorage.getCommonFriendsList(userId, friendId);
    }

    public Collection<User> getFriendsList(long userId) {
        User user = getUserById(userId);
        log.debug("Get list of friends for user {}", user);
        return userStorage.getFriendsList(userId);
    }

    public User getUserById(long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            log.warn("User with id {} doesn't exist", id);
            throw new UserNotFoundException(Long.toString(id));
        }
        log.debug("Get user with id: {}", id);
        return user;
    }

    public void deleteUser(long id) {
        if (!userStorage.deleteUser(userStorage.getById(id))) {
            throw new UserNotFoundException("User with id " + id + " is not found");
        }
    }

    public Collection<Film> getRecommendation(long id) {
        if (userStorage.getById(id) == null) {
            log.warn("User with id {} doesn't exist", id);
            throw new UserNotFoundException("User with id " + id + " is not found");
        }
        log.debug("Getting recommendation films for user " + id);
        return userStorage.getRecommendations(id);
    }
}
