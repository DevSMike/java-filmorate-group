package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.EventFilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventFilmDtoMapper {

    public static List<EventFilmDto> toEventsFilmDto(List<Event> events, List<Film> films) {
        List<Event> eventsForFilms = events.stream().filter(x -> x.getEventType().equals(EventTypes.RATE))
                .collect(Collectors.toList());
        List<EventFilmDto> result = new ArrayList<>();

        for (Event e : eventsForFilms) {
            Instant instant = Instant.ofEpochMilli(e.getTimestamp());
            Film currentFilm = films.stream().filter(x -> x.getId() == e.getEntityId()).findFirst().orElse(null);
            if (currentFilm == null) {
                continue;
            }
            result.add(EventFilmDto.builder()
                    .operation(e.getOperation())
                    .timestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                    .eventType(e.getEventType())
                    .filmRate(e.getFilmRate())
                    .description(currentFilm.getDescription())
                    .name(currentFilm.getName())
                    .releaseDate(currentFilm.getReleaseDate())
                    .build());
        }

        return result;
    }
}
