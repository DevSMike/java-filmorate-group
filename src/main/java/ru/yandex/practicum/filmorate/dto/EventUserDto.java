package ru.yandex.practicum.filmorate.dto;

import lombok.*;
import ru.yandex.practicum.filmorate.model.event.EventOperations;
import ru.yandex.practicum.filmorate.model.event.EventTypes;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventUserDto {

    LocalDateTime timestamp;
    EventTypes eventType;
    EventOperations operation;
    private String email;
    private String login;
    private String name;
}
