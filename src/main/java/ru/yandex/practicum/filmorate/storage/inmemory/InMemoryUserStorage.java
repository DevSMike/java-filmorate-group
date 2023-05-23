package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> database = new HashMap<>();
    private long id = 1;

    @Override
    public User addUser(User user) {
        long userId = getNextId();
        user.setId(userId);
        log.info("User added: " + user);
        database.put(user.getId(), user);
        return database.get(userId);
    }

    private long getNextId() {
        return id++;
    }

    @Override
    public User updateUser(User user) {
        Long userId = user.getId();
        if (!database.containsKey(userId)) {
            log.warn("There is no user in the database with id: " + userId);
            throw new UserNotFoundException("Wrong id");
        } else {
            log.debug("Update user id: " + userId);
            database.put(user.getId(), user);
            return database.get(userId);
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        log.debug("Get request /users");
        return database.values();
    }

    @Override
    public User getById(long id) {
        return database.get(id);
    }

    @Override
    public boolean deleteUser(User user) {
        return database.remove(user.getId()) != null;
    }

    @Override
    public Collection<Film> getRecommendations(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Map<Long, Integer>> getFilmsRates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> getCommonFriendsList(long userId, long friendId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> getFriendsList(long userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isExist(Long id) {
        return database.get(id) != null;
    }
}
