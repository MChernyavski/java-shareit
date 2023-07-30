package ru.practicum.shareit.exception;

public class BadRequestStateException extends RuntimeException {
    public BadRequestStateException(final String message) {
        super(message);
    }
}
