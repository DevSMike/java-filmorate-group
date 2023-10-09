package ru.yandex.practicum.filmorate.storage.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class DirectorDbStorage implements DirectorStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    static final RowMapper<Director> directorMapper = (rs, rowNum) -> new Director(rs.getLong("director_id"),
            rs.getString("director_name"));

    public DirectorDbStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Director> getAll() {
        String sql = "SELECT * FROM director";
        log.debug("Getting all directors from db");
        return jdbcTemplate.query(sql, directorMapper);
    }

    @Override
    public Optional<Director> getById(Long id) {
        String sql = "SELECT * FROM director WHERE director_id=:id";
        try {
            Director director = jdbcTemplate.queryForObject(sql, Map.of("id", id), directorMapper);
            log.debug("Getting director with id {}", id);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Empty sql response for director id: {}", id);
            return Optional.empty();
        }
    }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO director(director_name) " +
                "VALUES(:name)";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", director.getName());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, sqlParameterSource, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        long id = (long) Objects.requireNonNull(keys).get("director_id");
        log.debug("Director added with id: {}", id);
        return getById(id).orElseThrow(() -> new NotFoundException("Error creating director id " + id));
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE director SET director_name=:name WHERE director_id=:id";
        long id = director.getId();
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", director.getName());
        jdbcTemplate.update(sql, sqlParameterSource);
        log.debug("Director with id {} has been updated", id);
        return getById(id).orElseThrow(() -> new NotFoundException("Error creating director id " + id));

    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM director WHERE director_id=:id";
        if (jdbcTemplate.update(sql, Map.of("id", id)) > 0) {
            log.debug("Director {} has been deleted", id);
        } else {
            throw new NotFoundException("There is no director with id: " + id);
        }
    }
}
