package ru.yandex.practicum.filmorate.controllers.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.dto.UpdateFilmDirectorDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
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
    private final DirectorService directorService;

    @GetMapping("/add")
    public String addFilmForm(Model model) {
        model.addAttribute("film", new Film());
        model.addAttribute("allGenres", genreService.getAll());
        model.addAttribute("allMpa", mpaService.getAll());
        return "add-film";
    }

    @PostMapping("/add")
    public String addFilm(Film film) {
        log.info("film: {}", film);
        filmService.addFilm(film);
        return "redirect:/films";
    }

    @GetMapping("/add/director")
    public String addDirectorToFilmForm(Model model) {
        UpdateFilmDirectorDto form = new UpdateFilmDirectorDto();
        model.addAttribute("form", form);
        model.addAttribute("films", filmService.getAllFilms());
        model.addAttribute("directors", directorService.getAll());
        return "add-director-to-film";
    }

    @PostMapping("/add/director")
    public String addDirectorToFilm(@ModelAttribute("form") UpdateFilmDirectorDto form) {
        Film film = filmService.getFilmById(form.getFilmId());
        Director director = directorService.getById(form.getDirectorId());
        film.getDirectors().add(director);
        filmService.updateFilm(film);
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
        log.info("deleting film with id: {}", id);
        filmService.deleteFilm(id);
        return "redirect:/films";
    }

    @GetMapping("/director/films")
    public String getFilmForDirector(Model model) {
        model.addAttribute("directors", directorService.getAll());
        return "films-for-director";
    }

    @PostMapping("/director/films")
    public String createListFilmsForDirector(Model model, String id, String sortBy) {
        log.info("Getting films for director with id: {}", id);
        Collection<Film> films = filmService.getFilmsForDirector(Long.parseLong(id), sortBy);
        model.addAttribute("films", films);
        model.addAttribute("directors", directorService.getAll());
        return "films-for-director";
    }


}
