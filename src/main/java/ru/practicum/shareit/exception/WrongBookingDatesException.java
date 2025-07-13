package ru.practicum.shareit.exception;

public class WrongBookingDatesException extends RuntimeException {
    public WrongBookingDatesException(String message) {
        super(message);
    }
}
