package ru.yandex.practicum.filmorate.excption;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
