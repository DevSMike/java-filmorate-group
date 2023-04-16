DROP TABLE mpa IF EXISTS CASCADE;
DROP TABLE film IF EXISTS CASCADE;
DROP TABLE users IF EXISTS CASCADE;
DROP TABLE genre IF EXISTS CASCADE;
DROP TABLE director IF EXISTS CASCADE;
DROP TABLE film_genre IF EXISTS;
DROP TABLE user_friends IF EXISTS;
DROP TABLE film_likes IF EXISTS;
DROP TABLE review_likes IF EXISTS;
DROP TABLE review IF EXISTS;
DROP TABLE director_film IF EXISTS;
DROP TABLE event IF EXISTS;

CREATE TABLE mpa (
    mpa_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    mpa_name VARCHAR(20) NOT NULL
);

CREATE TABLE film (
    film_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_id INT,
    rate INT,
    CONSTRAINT fk_mpa_id FOREIGN KEY (mpa_id) REFERENCES mpa
);

CREATE TABLE users (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    login VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE genre (
    genre_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

CREATE TABLE film_genre (
    film_id BIGINT,
    genre_id INT,
    CONSTRAINT fk_genre_film_id FOREIGN KEY (film_id) REFERENCES film ON DELETE CASCADE,
    CONSTRAINT fk_genre_id FOREIGN KEY (genre_id) REFERENCES genre
);

CREATE TABLE user_friends (
    user_id BIGINT,
    friend_id BIGINT,
    status VARCHAR(20),
    CONSTRAINT fk_friends_user_id FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE,
    CONSTRAINT fk_friends_friend_id FOREIGN KEY (friend_id) REFERENCES users ON DELETE CASCADE
);

CREATE TABLE film_likes (
    film_id BIGINT,
    user_id BIGINT,
    CONSTRAINT fk_likes_film_id  FOREIGN KEY (film_id) REFERENCES film ON DELETE CASCADE,
    CONSTRAINT fk_likes_user_id FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content VARCHAR,
    is_positive BOOLEAN,
    user_id BIGINT REFERENCES users(user_id),
    film_id BIGINT REFERENCES film(film_id)
);

CREATE TABLE IF NOT EXISTS review_likes (
    review_id BIGINT REFERENCES review(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(user_id),
    is_like BOOLEAN
);

CREATE TABLE director (
    director_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    director_name VARCHAR(50)
);

CREATE TABLE director_film (
    director_id BIGINT,
    film_id BIGINT,
    CONSTRAINT fk_director_director_id FOREIGN KEY (director_id) REFERENCES director ON DELETE CASCADE,
    CONSTRAINT fk_director_film_id FOREIGN KEY (film_id) REFERENCES film ON DELETE CASCADE
);

CREATE TABLE event  (
    event_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    entity_id BIGINT,
    time_stamp BIGINT,
    event_type VARCHAR(6),
    operation VARCHAR(6)
)

