package ru.yandex.practicum.filmorate.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFilmDirectorDto {
    private Long filmId;
    private Long directorId;
}

