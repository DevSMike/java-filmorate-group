package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dto.EventFilmDto;
import ru.yandex.practicum.filmorate.dto.EventReviewDto;
import ru.yandex.practicum.filmorate.dto.EventUserDto;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.service.*;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.mapper.EventFilmDtoMapper.toEventsFilmDto;
import static ru.yandex.practicum.filmorate.mapper.EventReviewDtoMapper.toEventsReviewDto;
import static ru.yandex.practicum.filmorate.mapper.EventUserDtoMapper.toEventsUserDto;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/application")
public class ApplicationController {

    private final FilmService filmService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final GenreService genreService;
    private final EventService eventService;
    private final DirectorService directorService;


    private User currentUser;
    private long currentFilmId;

    @GetMapping("/login")
    public String loginUserForm(Model model) {
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "application-login";
    }

    @PostMapping("/login")
    public String loginUser(long userId) {
        currentUser = userService.getUserById(userId);
        return "redirect:/application/films";
    }

    @GetMapping("/films")
    public String getFilms(Model model) {
        Collection<Film> films = filmService.getAllFilms();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("friends", userService.getAllUsers().stream()
                .filter(x-> currentUser.getFriends().containsKey(x.getId()))
                .collect(Collectors.toList()));
        model.addAttribute("directors", directorService.getAll());
        model.addAttribute("films", films);
        return "application-films";
    }

    @GetMapping("/user/{id}")
    public String getByIdUserForm(@PathVariable("id") long id, Model model) {
        log.info("Get request for user: {}", id);
        List<User> users = new ArrayList<>(userService.getAllUsers());
        User user = userService.getUserById(id);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("getUserOnClickId", user.getId());
        model.addAttribute("userById", user);
        model.addAttribute("friends", users.stream().filter(x -> user.getFriends().containsKey(x.getId()))
                .collect(Collectors.toList()));
        model.addAttribute("commonFriends", userService.getCommonFriendsList(user.getId(), currentUser.getId()));
        model.addAttribute("commonFilms", filmService.getCommonFilms(id, currentUser.getId()));
        return "application-user-info";
    }

    @GetMapping("/user/login/{id}")
    public String getByIdCurrentUserForm(@PathVariable("id") long id, Model model) {
        log.info("Get request for user: {}", id);
        List<User> users = new ArrayList<>(userService.getAllUsers());
        User user = userService.getUserById(id);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("loginUserById", user);
        model.addAttribute("friends", users.stream().filter(x -> user.getFriends().containsKey(x.getId()))
                .collect(Collectors.toList()));
        return "application-login-user-info";
    }


    @PostMapping("/add/{id}/friends/{friendId}")
    public String addFriend(@PathVariable long id, @PathVariable long friendId, Model model) {
        log.info("Put request to add friend id: {} friendId: {}", id, friendId);
        userService.addFriend(id, friendId);
        return "redirect:/application/user/" + id;
    }

    @PostMapping("/delete/{id}/friends/{friendId}")
    public String removeFriend(@PathVariable long id, @PathVariable long friendId, Model model) {
        log.info("Delete request to remove friend id: {} friendId: {}", id, friendId);
        userService.removeFriend(id, friendId);
        return "redirect:/application/user/" + id;
    }


    @GetMapping("/film/{id}")
    public String getByIdFrom(@PathVariable("id") long id, Model model) {
        log.info("Get request for film: {}", id);
        Film film = filmService.getFilmById(id);
        currentFilmId = id;

        model.addAttribute("reviews", reviewService.getReviews().stream().filter(x -> x.getFilmId().equals(id))
                .collect(Collectors.toList()));
        model.addAttribute("authors", reviewService.getReviews().stream().filter(x -> x.getFilmId().equals(id))
                .map(Review::getUserId).collect(Collectors.toList()));
        Map<Long, User> users = new HashMap<>();
        for (User u : userService.getAllUsers()) {
            users.put(u.getId(), u);
        }
        model.addAttribute("users", users);

        model.addAttribute("currentFilmId", currentFilmId);
        model.addAttribute("film", film);
        model.addAttribute("filmRates", film.getRates().keySet());
        model.addAttribute("currentUser", currentUser);
        return "application-film-info";
    }

    @PostMapping("/film/rate/{filmId}/{userId}")
    public String addRate(@PathVariable("filmId") Long filmId, @PathVariable("userId") Long userId, @RequestParam("rate") Integer rate) {
        log.info("Put request to add rate {} to film: {} from user: {}", rate, filmId, userId);
        filmService.addRate(userId, filmId, rate);
        return "redirect:/application/film/" + currentFilmId;
    }

    @PostMapping("/film/rate/delete/{filmId}/{userId}")
    public String deleteRate(@PathVariable("filmId") Long filmId, @PathVariable("userId") Long userId) {
        log.info("Delete request to remove rate from film: {} from user: {}", filmId, userId);
        filmService.removeRate(userId, filmId);
        return "redirect:/application/film/" + currentFilmId;
    }

    @PostMapping("/review/add/{filmId}/{userId}")
    public String addReview(@PathVariable("filmId") Long filmId, @PathVariable("userId") long userId, @RequestParam("content") String content, @RequestParam(name = "isPositive", required = false) Boolean isPositive) {
        if (isPositive == null) {
            isPositive = false;
        }
        Review review = Review.builder()
                .content(content)
                .filmId(filmId)
                .userId(userId)
                .isPositive(isPositive)
                .build();
        reviewService.addReview(review);
        return "redirect:/application/film/{filmId}";
    }

    @PostMapping("/review/delete/{reviewId}")
    public String deleteReviewOnFilmInfo(@PathVariable("reviewId") long reviewId) {
        log.info("Delete request for review by id= {reviewId}");
        reviewService.deleteReview(reviewId);
        return "redirect:/application/film/" + currentFilmId;
    }

    @PostMapping("/film/{id}")
    public String getById(Model model) {
        Film film = filmService.getFilmById(currentFilmId);
        model.addAttribute("film", film);
        return "application-film-info";
    }

    @PostMapping("/review/like/add/{reviewId}/{userId}")
    public String likeReview(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Put request for review by id= {reviewId} (like)");
        reviewService.likeReview(reviewId, userId);
        return "redirect:/application/film/" + currentFilmId;
    }

    @PostMapping("/review/dislike/add/{reviewId}/{userId}")
    public String dislikeReview(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Delete request for review by id= {reviewId} (like)");
        reviewService.dislikeReview(reviewId, userId);
        return "redirect:/application/film/" + currentFilmId;
    }

    @DeleteMapping("/review/{reviewId}/dislike/delete")
    public void deleteDislike(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Delete request for review by id= {reviewId} (dislike)");
        reviewService.deleteDislike(reviewId, userId);
    }

    @DeleteMapping("/review/{reviewId}/like/delete")
    public void deleteLike(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Put request for review by id= {reviewId} (dislike)");
        reviewService.deleteLike(reviewId, userId);
    }

    @GetMapping("/films/popular")
    public String getPopularFilmsForm(Model model) {
        Collection<Genre> genres = genreService.getAll();
        model.addAttribute("genres", genres);
        return "films-popular";
    }

    @PostMapping("/films/popular")
    public String getTopFilms(@RequestParam(defaultValue = "10") Integer count,
                              @RequestParam(defaultValue = "0") Integer genreId,
                              @RequestParam(defaultValue = "0") Integer year, Model model) {
        log.info("Get request for top {} films , genreId: {}, year: {}", count, genreId, year);
        Collection<Film> topFilms = filmService.getTopFilms(count, genreId, year);
        model.addAttribute("films", topFilms);
        Collection<Genre> genres = genreService.getAll();
        model.addAttribute("genres", genres);
        return "films-popular";
    }

    @GetMapping("/films/search")
    public String getSearchForm(Model model) {
        List<String> searchBy = Arrays.asList("director", "title");
        model.addAttribute("searchBy", searchBy);
        return "films-search";
    }

    @PostMapping("/films/search")
    public String getSearchResult(@RequestParam("query") String query, @RequestParam("by") String by, Model model) {
        log.info("Get search request for films with query: {}, for fields {}", query, by);
        Collection<Film> films = filmService.getSearchResult(query, by);
        model.addAttribute("films", films);
        List<String> searchBy = Arrays.asList("director", "title");
        model.addAttribute("searchBy", searchBy);
        return "films-search";
    }

    @GetMapping("/users/feed")
    public String getUserEvents(Model model) {
        long id = currentUser.getId();
        log.info("Get request for userId {} events", id);
        Collection<Event> events = eventService.getUserEvents(id);

        List<EventUserDto> userEvents = toEventsUserDto(new ArrayList<>(events), new ArrayList<>(userService.getAllUsers()));
        List<EventFilmDto> filmEvents = toEventsFilmDto(new ArrayList<>(events), new ArrayList<>(filmService.getAllFilms()));
        List<EventReviewDto> reviewEvents = toEventsReviewDto(new ArrayList<>(events), new ArrayList<>(reviewService.getReviews()));

        model.addAttribute("reviews", reviewEvents);
        model.addAttribute("users", userEvents);
        model.addAttribute("films", filmEvents);

        return "user-feed";
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
