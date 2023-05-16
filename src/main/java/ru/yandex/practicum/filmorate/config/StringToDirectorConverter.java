package ru.yandex.practicum.filmorate.config;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

@Component
@RequiredArgsConstructor
public class StringToDirectorConverter implements Converter<String, Director> {
    private final DirectorService directorService;


    @Override
    public Director convert(String id) {
        Long directorId = Long.parseLong(id);
        return directorService.getById(directorId);
    }
}
