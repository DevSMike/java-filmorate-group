package ru.yandex.practicum.filmorate.controllers.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Post request for film");
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Put request for film");
        return filmService.updateFilm(film);
    }

    @GetMapping("/films")
    public Collection<Film> getAllFilms() {
        log.info("Get request for films");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        log.info("Get request for film: {}", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Put request to add like to film: {} from user: {}", id, userId);
        filmService.addLike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Delete request to remove like from film: {} from user: {}", id, userId);
        filmService.removeLike(userId, id);
    }

    @GetMapping("/popular")
    public Collection<Film> getTopFilms(@RequestParam(defaultValue = "10") Integer count,
                                        @RequestParam(defaultValue = "0") Integer genreId,
                                        @RequestParam(defaultValue = "0") Integer year) {
        log.info("Get request for top {} films , genreId: {}, year: {}", count, genreId, year);
        return filmService.getTopFilms(count, genreId, year);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable long filmId) {
        log.info("Delete request to remove film with id {}", filmId);
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/director/{id}")
    public Collection<Film> getFilmForDirector(@PathVariable Long id, @RequestParam String sortBy) {
        log.info("Get request for films by director with id: {} sorted by {}", id, sortBy);
        return filmService.getFilmsForDirector(id, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> getSearchResult(@RequestParam("query") @NotNull String query, @RequestParam("by") @NotNull String by) {
        log.info("Get search request for films with query: {}, for fields {}", query, by);
        return filmService.getSearchResult(query, by);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("Get common films for users: {} {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}