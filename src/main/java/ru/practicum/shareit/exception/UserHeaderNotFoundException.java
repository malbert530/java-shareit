package ru.practicum.shareit.exception;

public class UserHeaderNotFoundException extends RuntimeException {
    public UserHeaderNotFoundException(String message) {
        super(message);
    }
}
