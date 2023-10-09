package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.dto.EventReviewDto;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventReviewDtoMapper {

    public static List<EventReviewDto> toEventsReviewDto(List<Event> events, List<Review> reviews) {
        List<Event> eventsForUser = events.stream()
                .filter(x -> x.getEventType().equals(EventTypes.REVIEW) || x.getEventType().equals(EventTypes.LIKE))
                .collect(Collectors.toList());

        List<EventReviewDto> result = new ArrayList<>();

        for (Event e : eventsForUser) {
            Instant instant = Instant.ofEpochMilli(e.getTimestamp());
            Review currentReview = reviews.stream().filter(x -> x.getReviewId() == e.getEntityId()).findFirst().orElse(null);
            if (currentReview == null) {
                continue;
            }
            result.add(EventReviewDto.builder()
                    .operation(e.getOperation())
                    .timestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                    .eventType(e.getEventType())
                    .content(currentReview.getContent())
                    .build());
        }

        return result;
    }
}
