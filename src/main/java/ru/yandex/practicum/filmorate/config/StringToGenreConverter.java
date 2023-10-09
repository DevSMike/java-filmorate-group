package ru.yandex.practicum.filmorate.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

@Component
public class StringToGenreConverter implements Converter<String, Genre> {

    private final GenreStorage genreRepository;

    public StringToGenreConverter(GenreStorage genreRepository) {
        this.genreRepository = genreRepository;

    }

    @Override
    public Genre convert(String source) {
        return genreRepository.getById(Integer.parseInt(source))
                .orElseThrow(() -> new IllegalArgumentException("Invalid genre id: " + source));
    }

}
