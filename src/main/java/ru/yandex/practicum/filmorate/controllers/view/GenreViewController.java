package ru.yandex.practicum.filmorate.controllers.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@Controller
@RequestMapping("/genres")
public class GenreViewController {

    private final GenreService genreService;

    public GenreViewController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public String getAllGenres(Model model) {
        Collection<Genre> genres = genreService.getAll();
        model.addAttribute("genres", genres);
        log.info("Get request for getAllGenres");
        return "genres";
    }
}
