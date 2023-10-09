package ru.yandex.practicum.filmorate.controllers.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
@Slf4j
public class ReviewViewController {

    private final ReviewService reviewService;
    private final FilmService filmService;
    private final UserService userService;


    @GetMapping("/add")
    public String addReviewForm(Model model) {
        model.addAttribute("review", new Review());
        Collection<User> users = userService.getAllUsers();
        Collection<Film> films = filmService.getAllFilms();
        model.addAttribute("users", users);
        model.addAttribute("films", films);
        return "review-add";
    }

    @PostMapping("/add")
    public String addReview(Review review) {
        log.info("Post request for review(adding)");
        reviewService.addReview(review);
        return "redirect:/reviews";
    }

    @GetMapping()
    public String getAllReviews(Model model) {
        log.info("Getting all reviews");
        Collection<Review> reviews = reviewService.getReviews();
        model.addAttribute("reviews", reviews);
        return "reviews";
    }

    @PostMapping("/delete")
    public Boolean deleteReview(@RequestParam Long id) {
        log.info("Delete request for review by id= {id}");
        return reviewService.deleteReview(id);
    }

    @GetMapping("/useful")
    public String getUsefulReviewsForm(Model model) {
        Collection<Film> films = filmService.getAllFilms();
        model.addAttribute("films", films);
        return "films-useful-review";
    }

    @PostMapping("/useful")
    public String getUsefulReviews(@RequestParam Long filmId,
                                   @RequestParam(defaultValue = "10") Long count, Model model) {
        log.info("Get request for most useful reviews");
        Collection<Review> reviews = reviewService.getUsefulReviews(filmId, count);
        model.addAttribute("reviews", reviews);
        Collection<Film> films = filmService.getAllFilms();
        model.addAttribute("films", films);
        return "films-useful-review";
    }

    @GetMapping("/likes")
    public String getLikesReviewForm(Model model) {
        Collection<Review> reviews = reviewService.getReviews();
        model.addAttribute("reviews", reviews);
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "review-likes";
    }

    @PostMapping("/likes/add")
    public String likeReview(Long reviewId, Long userId) {
        log.info("Put request for review by id= {reviewId} (like)");
        reviewService.likeReview(reviewId, userId);
        return "redirect:/reviews";
    }

    @PostMapping("/likes/delete")
    public String deleteLike(Long reviewId, Long userId) {
        log.info("Put request for review by id= {reviewId} (dislike)");
        reviewService.deleteLike(reviewId, userId);
        return "redirect:/reviews";
    }

    @GetMapping("/dislikes")
    public String getDislikesReviewForm(Model model) {
        Collection<Review> reviews = reviewService.getReviews();
        model.addAttribute("reviews", reviews);
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "review-dislikes";
    }

    @PostMapping("/dislike/add")
    public String dislikeReview(Long reviewId, Long userId) {
        log.info("Delete request for review by id= {reviewId} (like)");
        reviewService.dislikeReview(reviewId, userId);
        return "redirect:/reviews";
    }

    @PostMapping("/dislike/delete")
    public String deleteDislike(Long reviewId, Long userId) {
        log.info("Delete request for review by id= {reviewId} (dislike)");
        reviewService.deleteDislike(reviewId, userId);
        return "redirect:/reviews";
    }
}
