package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperations;
import ru.yandex.practicum.filmorate.model.event.EventTypes;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.Instant;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final EventService eventService;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, UserService userService, EventService eventService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.eventService = eventService;
    }

    public Film addFilm(Film film) {
        log.debug("Adding film " + film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.debug("Updating film " + film);
        if (film.getDirectors() != null) {
            return filmStorage.updateFilm(FilmMapper.toUpdate(film, filmStorage.getById(film.getId())));
        }
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        log.debug("Getting all films");
        return filmStorage.getAllFilms();
    }

    public void addLike(long userId, long filmId) {
        userService.isExist(userId);
        Film film = getFilmById(filmId);
        log.debug("Adding like to film: {} from user: {}", film, userId);
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        eventService.addEvent(Event.builder()
                .userId(userId)
                .entityId(filmId)
                .eventType(EventTypes.LIKE)
                .operation(EventOperations.ADD)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    public void removeLike(long userId, long filmId) {
        userService.isExist(userId);
        Film film = getFilmById(filmId);
        log.debug("Removing like to film: {} from user: {}", film, userId);
        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        eventService.addEvent(Event.builder()
                .userId(userId)
                .entityId(filmId)
                .eventType(EventTypes.LIKE)
                .operation(EventOperations.REMOVE)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    public Collection<Film> getTopFilms(Integer size, Integer genreId, Integer year) {
        log.debug("Get top {} films", size);
        return filmStorage.getTopFilms(size, genreId, year);
    }

    public Film getFilmById(long filmId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) {
            log.warn("Film with id {} doesn't exist", filmId);
            throw new FilmNotFoundException(Long.toString(filmId));
        }
        log.debug("Get film with id: {}", filmId);
        return film;
    }

    public Collection<Film> getFilmsForDirector(Long id, String sortBy) {
        log.debug("Get films by director with id: {} sorted by {}", id, sortBy);
        return filmStorage.getFilmsForDirectorSorted(id, sortBy);
    }

    public void deleteFilm(long id) {
        if (!filmStorage.deleteFilm(filmStorage.getById(id))) {
            throw new FilmNotFoundException("Film with id " + id + " not found!");
        }
    }

    public Collection<Film> getSearchResult(String query, String by) {
        log.debug("Get search request for films with query: {}, for fields {}", query, by);
        return filmStorage.getSearchResult(query, by);
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        userService.isExist(userId);
        userService.isExist(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void isExist(Long id) {
        log.debug("Checking is film with id {} exists", id);
        boolean isExists = filmStorage.isExist(id);
        if (!isExists) {
            log.warn("Film with id {} doesn't exist", id);
            throw new FilmNotFoundException(Long.toString(id));
        }
    }
}
