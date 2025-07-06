package ru.practicum.shareit.exception;

public class StateNotValidException extends RuntimeException {
    public StateNotValidException(String message) {
        super(message);
    }
}
