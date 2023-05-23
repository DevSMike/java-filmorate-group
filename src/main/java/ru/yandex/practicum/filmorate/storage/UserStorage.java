package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();

    User getById(long id);

    boolean deleteUser(User user);

    Collection<Film> getRecommendations(long id);

    Map<Long, Map<Long, Integer>> getFilmsRates();

    Collection<User> getCommonFriendsList(long userId, long friendId);

    Collection<User> getFriendsList(long userId);

    boolean isExist(Long id);
}
