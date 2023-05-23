package ru.yandex.practicum.filmorate.exceptions;

public class RateIsIncorrectException extends RuntimeException {

    public RateIsIncorrectException(String message) {
        super(message);
    }
}
