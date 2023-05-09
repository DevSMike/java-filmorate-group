package ru.yandex.practicum.filmorate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final StringToGenreConverter stringToGenreConverter;

    public WebMvcConfig(StringToGenreConverter stringToGenreConverter) {
        this.stringToGenreConverter = stringToGenreConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToGenreConverter);
    }
}
