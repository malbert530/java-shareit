package ru.practicum.shareit.exception;

public class UserNotOwnerException extends RuntimeException {
    public UserNotOwnerException(String message) {
        super(message);
    }
}
