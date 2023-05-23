package ru.yandex.practicum.filmorate.controllers.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getUsefulReviews(@RequestParam(defaultValue = "0") Long filmId,
                                         @RequestParam(defaultValue = "10") Long count) {

        if (filmId == 0) {
            log.info("Get request for all reviews");
            return reviewService.getReviews();
        }
        log.info("Get request for most useful reviews");
        return reviewService.getUsefulReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Get request for review by id= {id}");
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Post request for review(adding)");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Put request for review(update)");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteReview(@PathVariable Long id) {
        log.info("Delete request for review by id= {id}");
        return reviewService.deleteReview(id);
    }


    @PutMapping("/{id}/like/{userId}")
    public Review likeReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Put request for review by id= {id} (like)");
        return reviewService.likeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review dislikeReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Put request for review by id= {id} (dislike)");
        return reviewService.dislikeReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Delete request for review by id= {id} (like)");
        return reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Delete request for review by id= {id} (dislike)");
        return reviewService.deleteDislike(id, userId);
    }
}
