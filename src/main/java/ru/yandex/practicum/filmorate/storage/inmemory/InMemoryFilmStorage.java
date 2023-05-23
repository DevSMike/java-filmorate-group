package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

@Repository("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Long, Film> database = new HashMap<>();
    private long id = 1;

    @Override
    public Film addFilm(Film film) {
        long filmId = getNextId();
        film.setId(filmId);
        database.put(filmId, film);
        log.debug("Film added: " + film);
        return database.get(filmId);
    }

    private long getNextId() {
        return id++;
    }

    @Override
    public Film updateFilm(Film film) {
        Long filmId = film.getId();
        if (!database.containsKey(filmId)) {
            log.warn("Film id is not in db. Id: " + filmId);
            throw new FilmNotFoundException("Incorrect film id for update");
        } else {
            database.put(filmId, film);
            log.debug("Film updated: " + film);
            return database.get(filmId);
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        return database.values();
    }

    @Override
    public Collection<Film> getTopFilms(int size, int genreId, int year) {
        return getAllFilms().stream()
                .sorted(Comparator.comparingDouble(Film::getAvgRate).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteFilm(Film film) {
        return database.remove(film.getId()) != null;
    }

    @Override
    public Film getById(long id) {
        return database.get(id);
    }

    @Override
    public Collection<Film> getFilmsForDirectorSorted(Long id, String sortBy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Film> getSearchResult(String query, String by) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Film> getCommonFilms(long uid, long fid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isExist(Long id) {
        return database.get(id) != null;
    }
}