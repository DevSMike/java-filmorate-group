package ru.yandex.practicum.filmorate.controllers.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@Controller
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmsViewController {

    private final FilmService filmService;
    private final GenreService genreService;
    private final MpaService mpaService;

    @GetMapping("/add")
    public String addFilmForm(Model model) {
        model.addAttribute("film", new Film());
        model.addAttribute("allGenres", genreService.getAll());
        model.addAttribute("allMpa", mpaService.getAll());
        return "add-film";
    }

    @PostMapping("/add")
    public String addFilm( Film film) {
        log.info("film: {}", film);
        filmService.addFilm(film);
        return "redirect:/films";
    }

    @GetMapping()
    public String getAllFilms(Model model) {
        Collection<Film> films = filmService.getAllFilms();
        model.addAttribute("films", films);
        return "films";
    }

    @PostMapping("/delete")
    public String deleteFilm(@RequestParam("id") long id) {
        filmService.deleteFilm(id);
        return "redirect:/films";
    }
}
