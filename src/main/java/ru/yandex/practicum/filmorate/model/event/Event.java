package ru.yandex.practicum.filmorate.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@Builder
@Data
public class Event {

    long eventId;
    @NonNull
    final long userId;
    @NonNull
    final long entityId;
    @NonNull
    final long timestamp;
    @NonNull
    final EventTypes eventType;
    @NonNull
    final EventOperations operation;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    final int filmRate;
}

