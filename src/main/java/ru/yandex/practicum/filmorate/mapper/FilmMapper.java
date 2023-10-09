package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.Film;

public class FilmMapper {

    public static Film toUpdate (Film newFilm, Film oldFilm) {
        return Film.builder()
                .id(newFilm.getId() != null ? newFilm.getId() : oldFilm.getId())
                .name(newFilm.getName() != null ? newFilm.getName() : oldFilm.getName())
                .mpa(newFilm.getMpa() != null ? newFilm.getMpa() : oldFilm.getMpa())
                .description(newFilm.getDescription() != null ? newFilm.getDescription() : oldFilm.getDescription())
                .duration(newFilm.getDuration() != null ? newFilm.getDuration() : oldFilm.getDuration())
                .genres(newFilm.getGenres() != null ? newFilm.getGenres() : oldFilm.getGenres())
                .releaseDate(newFilm.getReleaseDate() != null ? newFilm.getReleaseDate() : oldFilm.getReleaseDate())
                .rates(newFilm.getRates() != null ? newFilm.getRates() : oldFilm.getRates())
                .directors(newFilm.getDirectors() != null ? newFilm.getDirectors() : oldFilm.getDirectors())
                .build();
    }

}
