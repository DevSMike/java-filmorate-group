package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperations;
import ru.yandex.practicum.filmorate.model.event.EventTypes;
import ru.yandex.practicum.filmorate.storage.EventStorage;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
@Component
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO event (user_id, entity_id, time_stamp, event_type, operation, rate) " +
                "VALUES (?, ?, ?, ?, ?, ?);";
        jdbcTemplate.update(sql, event.getUserId(), event.getEntityId(), event.getTimestamp(),
                event.getEventType().toString(), event.getOperation().toString(), event.getFilmRate());
    }

    @Override
    public Event getEventById(long id) {
        String sql = "SELECT * FROM event WHERE event_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeEvent(rs), id);
    }

    @Override
    public Collection<Event> getUserEvents(long id) {
        String sql = "SELECT * FROM event WHERE user_id = ?;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeEvent(rs), id);
    }

    private Event makeEvent(ResultSet rs) throws SQLException {
        return Event.builder()
                .eventId(rs.getInt("event_id"))
                .userId(rs.getInt("user_id"))
                .entityId(rs.getInt("entity_id"))
                .timestamp(rs.getLong("time_stamp"))
                .eventType(EventTypes.valueOf(rs.getString("event_type")))
                .operation(EventOperations.valueOf(rs.getString("operation")))
                .filmRate(rs.getInt("rate"))
                .build();
    }
}
