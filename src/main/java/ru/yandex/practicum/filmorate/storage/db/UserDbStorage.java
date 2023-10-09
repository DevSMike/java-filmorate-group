package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.yandex.practicum.filmorate.storage.db.FilmDbStorage.SELECT_ALL_FILMS_WITH_GENRES_LIKES_AND_DIRECTORS;
import static ru.yandex.practicum.filmorate.storage.db.FilmDbStorage.filmWithGenresAndLikesExtractor;

@Repository("UserDbStorage")
@Primary
@Slf4j
public class UserDbStorage implements UserStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final String SELECT_ALL_USERS = "SELECT u.user_id, email, login, name, birthday, friend_id, status " +
            "FROM users u " +
            "LEFT JOIN user_friends f ON u.user_id = f.user_id ";

    public UserDbStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (:email, :login, :name, :birthday)";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, sqlParameterSource, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        long id = (long) Objects.requireNonNull(keys).get("user_id");
        log.debug("User created with id: " + id);
        Map<Long, String> friends = user.getFriends();
        if (friends.size() > 0) {
            sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (:id, :friend_id, :status)";
            log.debug("Set friends for user: " + id);
            SqlParameterSource[] params = friends.entrySet().stream()
                    .map(friend -> new MapSqlParameterSource().addValue("friend_id", friend.getKey())
                            .addValue("status", friend.getValue())
                            .addValue("id", user.getId()))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
        }
        return getById(id);
    }

    @Override
    public User updateUser(User user) {
        Long id = user.getId();
        try {
            jdbcTemplate.queryForObject("SELECT user_id FROM users WHERE user_id = :id",
                    Map.of("id", id), Long.class);
            log.debug("Updating user with id: " + id);
        } catch (IncorrectResultSizeDataAccessException e) {
            log.warn("There is no user in the database with id: " + id);
            throw new UserNotFoundException("Wrong id");
        }
        String sql = "UPDATE users SET email = :email, login = :login, name = :name, birthday = :birthday " +
                "WHERE user_id = :id";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());
        jdbcTemplate.update(sql, sqlParameterSource);
        Map<Long, String> friends = user.getFriends();
        jdbcTemplate.update("DELETE FROM user_friends WHERE user_id = :id", Map.of("id", user.getId()));
        if (friends.size() > 0) {
            log.debug("Updating friends for user with id: " + id);
            sql = "INSERT INTO user_friends (user_id, friend_id, status) VALUES (:id, :friend_id, :status)";
            SqlParameterSource[] params = friends.entrySet().stream()
                    .map(friend -> new MapSqlParameterSource().addValue("friend_id", friend.getKey())
                            .addValue("status", friend.getValue())
                            .addValue("id", user.getId()))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
        }
        return getById(user.getId());
    }

    @Override
    public Collection<User> getAllUsers() {
        log.debug("Getting all users");
        return jdbcTemplate.query(SELECT_ALL_USERS, new UserWithFriendsMapper());
    }

    @Override
    public User getById(long id) {
        String sql = SELECT_ALL_USERS + " WHERE u.user_id = :id";
        List<User> userList = jdbcTemplate.query(sql, Map.of("id", id), new UserWithFriendsMapper());
        if (userList.isEmpty()) {
            log.warn("Nof found user for id: " + id);
            return null;
        }
        return userList.get(0);
    }

    @Override
    public boolean deleteUser(User user) {
        String sqlQuery = "DELETE FROM users WHERE user_id = :id";
        log.debug("Delete user with id: " + user.getId());
        return jdbcTemplate.update(sqlQuery, Map.of("id", user.getId())) > 0;
    }

    private static final class UserWithFriendsMapper implements ResultSetExtractor<List<User>> {
        @Override
        public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, User> map = new HashMap<>();
            User user;
            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                user = map.computeIfAbsent(userId, id -> {
                    try {
                        String email = rs.getString("email");
                        String login = rs.getString("login");
                        LocalDate birthday = rs.getDate("birthday").toLocalDate();
                        User newUser = new User(email, login, birthday);
                        newUser.setId(rs.getLong("user_id"));
                        newUser.setName(rs.getString("name"));
                        return newUser;
                    } catch (SQLException e) {
                        log.warn("SQL Exception for user: " + id);
                        throw new RuntimeException("Bad sql request" + e);
                    }
                });
                Map<Long, String> friendMap = user.getFriends();
                Long friendId = rs.getLong("friend_id");
                if (friendId != 0) {
                    friendMap.put(friendId, rs.getString("status"));
                }
            }
            return new ArrayList<>(map.values());
        }
    }

    @Override
    public Collection<Film> getRecommendations(long id) {
        String sql = SELECT_ALL_FILMS_WITH_GENRES_LIKES_AND_DIRECTORS + " WHERE f.film_id IN (" +
                "SELECT f.film_id FROM film_rates AS f " +
                "JOIN (SELECT f3.user_id FROM film_rates AS f2 " +
                "LEFT JOIN film_rates AS f3 ON f2.film_id = f3.film_id WHERE f2.user_id = :id AND f3.user_id <> f2.user_id " +
                "GROUP BY f3.user_id ORDER BY COUNT(f3.film_id) DESC LIMIT 1) AS f1 ON f.user_id = f1.user_id " +
                "WHERE f.film_rate >= 6 AND f.film_id NOT IN (SELECT film_id FROM film_rates WHERE user_id = :id))";
        log.debug("Getting recommendation films for user " + id);
        return jdbcTemplate.query(sql, Map.of("id", id), filmWithGenresAndLikesExtractor);
    }

    @Override
    public Map<Long, Map<Long, Integer>> getFilmsRates() {
        String sql = "SELECT user_id, film_id, film_rate FROM film_rates";
        Map<Long, Map<Long, Integer>> rates = new HashMap<>();
        log.debug("Получаем оценки ко всем фильмам из film_rates");
        jdbcTemplate.getJdbcTemplate().query(sql, (rs, rowNumber) ->
        {
            Long fid = rs.getLong("film_id");
            Long uid = rs.getLong("user_id");
            Integer rate = rs.getInt("film_rate");
            if (!rates.containsKey(uid)) {
                rates.put(uid, new HashMap<>());
            }
            rates.get(uid).put(fid, rate);
            return null;
        });
        return rates;
    }

    @Override
    public Collection<User> getCommonFriendsList(long userId, long friendId) {
        String sql = SELECT_ALL_USERS + "WHERE u.user_id IN " +
                "(SELECT friend_id FROM user_friends WHERE user_id = :userId " +
                "INTERSECT " +
                "SELECT friend_id FROM user_friends WHERE user_id = :friendId)";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);
        log.debug("Getting common friends list for user {} and friend {}", userId, friendId);
        return jdbcTemplate.query(sql, sqlParameterSource, new UserWithFriendsMapper());
    }

    @Override
    public Collection<User> getFriendsList(long userId) {
        String sql = SELECT_ALL_USERS + "WHERE u.user_id IN " +
                "(SELECT friend_id FROM user_friends WHERE user_id = :userId)";
        log.debug("Get friend list for user {}", userId);
        return jdbcTemplate.query(sql, Map.of("userId", userId), new UserWithFriendsMapper());
    }

    @Override
    public boolean isExist(Long id) {
        String sql = "SELECT EXISTS(SELECT * FROM users WHERE user_id = :id)";
        log.debug("Checking existence of user with id {}", id);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("id", id), Boolean.class));
    }
}
