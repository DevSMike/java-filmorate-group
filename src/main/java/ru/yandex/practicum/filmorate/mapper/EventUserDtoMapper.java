package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.EventUserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventUserDtoMapper {

    public static List<EventUserDto> toEventsUserDto(List<Event> events, List<User> users) {
        List<Event> eventsForUser = events.stream().filter(x -> x.getEventType().equals(EventTypes.FRIEND))
                .collect(Collectors.toList());
        List<EventUserDto> result = new ArrayList<>();

        for (Event e : eventsForUser) {
            Instant instant = Instant.ofEpochMilli(e.getTimestamp());
            User currentUser = users.stream().filter(x -> x.getId() == e.getEntityId()).findFirst().orElse(null);
            if (currentUser == null) {
                continue;
            }
            result.add(EventUserDto.builder()
                    .login(currentUser.getLogin())
                    .operation(e.getOperation())
                    .timestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                    .email(currentUser.getEmail())
                    .name(currentUser.getEmail())
                    .eventType(e.getEventType())
                    .build());
        }

        return result;
    }
}
