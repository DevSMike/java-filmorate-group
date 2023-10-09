package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Review> getReviews() {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,\n" +
                "SUM(CASE\n" +
                "WHEN rl.is_like IS TRUE THEN 1 \n" +
                "WHEN rl.is_like IS FALSE THEN -1 " +
                "ELSE 0 END) AS useful\n" +
                "FROM review r  \n" +
                "LEFT JOIN REVIEW_LIKES rl ON r.ID = rl.REVIEW_ID \n" +
                "GROUP BY r.ID\n" +
                "ORDER BY useful DESC ");
        List<Review> reviews = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = convertSql(reviewRows);
            reviews.add(review);
        }
        return reviews;
    }

    @Override
    public Review getReviewById(Long id) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,\n" +
                "SUM(CASE\n" +
                "WHEN rl.is_like IS TRUE THEN 1 \n" +
                "WHEN rl.is_like IS FALSE THEN -1 " +
                "ELSE 0 END) AS useful\n" +
                "FROM review r  \n" +
                "LEFT JOIN REVIEW_LIKES rl ON r.id = rl.review_id \n" +
                "WHERE r.id = ? " +
                "GROUP BY r.id", id);
        if (reviewRows.next()) {
            return convertSql(reviewRows);
        } else {
            throw new NotFoundException("REVIEW NOT FOUND");
        }
    }

    @Override
    public Review addReview(Review review) {
        String sqlQuery = "INSERT INTO review(content, is_positive, user_id, film_id) "
                + "VALUES (?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setLong(3, review.getUserId());
            statement.setLong(4, review.getFilmId());
            return statement;
        }, kh);
        Map<String, Object> keys = kh.getKeys();
        long id = (long) Objects.requireNonNull(keys).get("id");
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sqlQuery = "UPDATE review " +
                "SET content = ?, is_positive = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    @Override
    public Boolean deleteReview(Long id) {
        String sqlDelete = "DELETE FROM review WHERE id = ?";
        return jdbcTemplate.update(sqlDelete, id) > 0;
    }

    //todo оптимизация на уровне запроса, а не сервиса
    @Override
    public List<Review> getMostUsefulReviews(Long filmId, Long count) {
        String sqlUseful = "SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id,\n" +
                "SUM(CASE\n" +
                "WHEN rl.IS_LIKE IS TRUE THEN 1 \n" +
                "WHEN rl.IS_LIKE IS FALSE THEN -1 " +
                "ELSE 0 END) AS useful\n" +
                "FROM review r  \n" +
                "LEFT JOIN REVIEW_LIKES rl ON r.id = rl.review_id \n" +
                "WHERE r.film_id = ? " +
                "GROUP BY r.id\n" +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(sqlUseful, filmId, count);
        List<Review> reviewList = new ArrayList<>();
        while (reviewRows.next()) {
            Review review = convertSql(reviewRows);
            reviewList.add(review);
        }
        return reviewList;
    }

    @Override
    public Review likeReview(Long id, Long userId) {
        String sqlInsertLikes = "INSERT INTO review_likes (review_id, user_id, is_like) "
                + "VALUES (?, ?, ?) ";
        jdbcTemplate.update(sqlInsertLikes, id, userId, true);
        return getReviewById(id);
    }

    @Override
    public Review dislikeReview(Long id, Long userId) {
        String sqlInsertLikes = "INSERT INTO review_likes (review_id, user_id, is_like) "
                + "VALUES (?, ?, ?) ";
        jdbcTemplate.update(sqlInsertLikes, id, userId, false);
        return getReviewById(id);
    }

    @Override
    public Review deleteLike(Long id, Long userId) {
        String sqlInsertLikes = "DELETE FROM review_likes WHERE review_id = ? " +
                "AND user_id = ? AND is_like IS TRUE ";
        jdbcTemplate.update(sqlInsertLikes, id, userId);
        return getReviewById(id);
    }

    @Override
    public Review deleteDislike(Long id, Long userId) {
        String sqlInsertLikes = "DELETE FROM review_likes WHERE review_id = ? " +
                "AND user_id = ? AND is_like IS FALSE ";
        jdbcTemplate.update(sqlInsertLikes, id, userId);
        return getReviewById(id);
    }

    private Review convertSql(SqlRowSet rs) {
        return Review.builder()
                .reviewId(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
