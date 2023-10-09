package ru.yandex.practicum.filmorate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StringToGenreConverter stringToGenreConverter;
    private final StringToDirectorConverter stringToDirectorConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToGenreConverter);
        registry.addConverter(stringToDirectorConverter);
    }
}
