package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;

import static ru.yandex.practicum.filmorate.storage.db.DirectorDbStorage.directorMapper;
import static ru.yandex.practicum.filmorate.storage.db.GenreDbStorage.genreMapper;
import static ru.yandex.practicum.filmorate.storage.db.MpaDbStorage.mpaMapper;

@Repository("FilmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL_FILMS_WITH_GENRES_LIKES_AND_DIRECTORS = "SELECT f.name AS f_name, description, " +
            "release_date, duration, rate, f.film_id, m.mpa_id, m.mpa_name, g.genre_id, g.name, l.user_id, " +
            "d.director_id, d.director_name " +
            "FROM film AS f " +
            "LEFT JOIN mpa AS m ON f.mpa_id=m.mpa_id " +
            "LEFT JOIN film_genre AS fg ON f.film_id=fg.film_id " +
            "LEFT JOIN genre AS g ON fg.genre_id=g.genre_id " +
            "LEFT JOIN film_likes AS l ON f.film_id=l.film_id " +
            "LEFT JOIN director_film AS fd ON f.film_id=fd.film_id " +
            "LEFT JOIN director AS d ON fd.director_id=d.director_id";

    private final ResultSetExtractor<List<Film>> filmWithGenresAndLikesExtractor = rs -> {
        Map<Long, Film> filmMap = new LinkedHashMap<>();
        Film film;
        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            film = filmMap.get(filmId);
            if (film == null) {
                String name = rs.getString("f_name");
                String description = rs.getString("description");
                LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
                Integer duration = rs.getInt("duration");
                film = new Film(name, description, releaseDate, duration);
                film.setRate(rs.getInt("rate"));
                film.setId(rs.getLong("film_id"));
                film.setMpa(mpaMapper.mapRow(rs, 0));
                filmMap.put(filmId, film);
            }
            Set<Genre> genres = film.getGenres();
            if (rs.getInt("genre_id") != 0) {
                genres.add(genreMapper.mapRow(rs, 0));
            }
            Set<Long> likes = film.getLikes();
            Long userId = rs.getLong("user_id");
            if (userId != 0) {
                likes.add(userId);
            }
            Set<Director> directors = film.getDirectors();
            if (rs.getLong("director_id") != 0) {
                directors.add(directorMapper.mapRow(rs, 0));
            }
        }
        return new ArrayList<>(filmMap.values());
    };

    public FilmDbStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO film(name, description, release_date, duration, rate, mpa_id) " +
                "VALUES (:name, :desc, :release_date, :duration, :rate, :mpa)";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("desc", film.getDescription())
                .addValue("release_date", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("rate", film.getLikes().size())
                .addValue("mpa", film.getMpa().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, sqlParameterSource, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        log.debug("Film added with id: " + id);
        Set<Long> likes = film.getLikes();
        if (likes != null && !likes.isEmpty()) {
            log.debug("Update likes for film with id: " + id);
            sql = "INSERT INTO film_likes (film_id, user_id) VALUES (:id, :user)";
            SqlParameterSource[] params = likes.stream()
                    .map(likeId -> new MapSqlParameterSource().addValue("user", likeId)
                            .addValue("id", id))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
        }
        Set<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            log.debug("Update genres for film with id: " + id);
            sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (:id, :genre)";
            SqlParameterSource[] params = genres.stream()
                    .map(genre -> new MapSqlParameterSource().addValue("genre", genre.getId())
                            .addValue("id", id))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
        }
        return getById(id);
    }

    @Override
    public Film updateFilm(Film film) {
        long id = film.getId();
        try {
            jdbcTemplate.queryForObject("SELECT film_id FROM film WHERE film_id=:id", Map.of("id", id), Long.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            log.warn("There is no film in the database with id: " + id);
            throw new UserNotFoundException("Wrong id");
        }
        String sql = "UPDATE film SET film_id=:id, name=:name, description=:desc, release_date=:release_date, " +
                "duration=:duration, rate=:rate, mpa_id=:mpa WHERE film_id=:id";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("desc", film.getDescription())
                .addValue("name", film.getName())
                .addValue("release_date", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("rate", film.getLikes().size())
                .addValue("mpa", film.getMpa().getId());
        jdbcTemplate.update(sql, sqlParameterSource);
        log.debug("Update film with id: " + id);
        Set<Long> likes = film.getLikes();
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id=:id", Map.of("id", id));
        if (likes != null && !likes.isEmpty()) {
            sql = "INSERT INTO film_likes (film_id, user_id) VALUES(:id, :user)";
            log.debug("Updating likes for film with id: " + id);
            SqlParameterSource[] params = likes.stream()
                    .map(likeId -> new MapSqlParameterSource().addValue("user", likeId)
                            .addValue("id", id))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
        }
        Set<Genre> genres = film.getGenres();
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = :id", Map.of("id", id));
        if (genres != null && !genres.isEmpty()) {
            sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (:id, :genre)";
            log.debug("Updating genres for film with id: " + id);
            SqlParameterSource[] params = genres.stream()
                    .map(genre -> new MapSqlParameterSource().addValue("genre", genre.getId())
                            .addValue("id", id))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate(sql, params);
        }
        return getById(film.getId());
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.debug("Getting all films");
        return jdbcTemplate.query(SELECT_ALL_FILMS_WITH_GENRES_LIKES_AND_DIRECTORS, filmWithGenresAndLikesExtractor);
    }

    @Override
    public Collection<Film> getTopFilms(int size, int genreId, int year) {
        log.debug("Getting top " + size + " films");
        Map<String, Integer> keys = new HashMap<>();
        String limitTop = "";
        if (genreId == 0 && year == 0) {
            limitTop = " WHERE f.film_id IN (SELECT film_id FROM film ORDER BY rate DESC LIMIT :size) ORDER BY rate DESC ";
            keys.put("size", size);
        }
        if (year != 0 && genreId == 0) {
            limitTop = " WHERE f.film_id IN " +
                    "(SELECT film_id FROM film WHERE EXTRACT(YEAR FROM f.RELEASE_DATE) = :year ORDER BY rate DESC LIMIT :size) ORDER BY rate DESC ";
            keys.put("year", year);
            keys.put("size", size);
        }
        if (genreId != 0 && year == 0) {
            limitTop = " WHERE f.film_id IN " +
                    "(SELECT film_id FROM film WHERE g.GENRE_ID = :genreId ORDER BY rate DESC LIMIT :size ) " +
                    "ORDER BY rate DESC ";
            keys.put("genreId", genreId);
            keys.put("size", size);
        }
        if (year != 0 && genreId != 0) {
            limitTop = " WHERE f.film_id IN (SELECT film_id FROM film " +
                    "WHERE g.GENRE_ID = :genreId AND EXTRACT(YEAR FROM f.RELEASE_DATE) = :year  " +
                    "ORDER BY rate DESC LIMIT :size) ORDER BY rate DESC";
            keys.put("genreId", genreId);
            keys.put("year", year);
            keys.put("size", size);
        }
        return jdbcTemplate.query(SELECT_ALL_FILMS_WITH_GENRES_LIKES_AND_DIRECTORS + limitTop, keys,
                filmWithGenresAndLikesExtractor);
    }

    @Override
    public boolean deleteFilm(Film film) {
        String sqlQuery = "DELETE FROM film WHERE film_id = :id";
        log.debug("Delete film with id: " + film.getId());
        return jdbcTemplate.update(sqlQuery, Map.of("id", film.getId())) > 0;
    }

    @Override
    public Film getById(long id) {
        String sql = SELECT_ALL_FILMS_WITH_GENRES_LIKES_AND_DIRECTORS + " WHERE f.film_id=:id";
        log.debug("Getting film by id: " + id);
        List<Film> film = jdbcTemplate.query(sql, Map.of("id", id), filmWithGenresAndLikesExtractor);
        if (film.isEmpty()) {
            log.warn("Film not found for id: " + id);
            return null;
        }
        return film.get(0);
    }
}