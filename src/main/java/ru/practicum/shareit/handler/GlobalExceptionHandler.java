package ru.practicum.shareit.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.apiError.ApiError;
import ru.practicum.shareit.exception.*;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFound(UserNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleItemNotFound(ItemNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleBookingNotFound(BookingNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.NOT_FOUND.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleValidation(ValidationException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.CONFLICT.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArgumentNotValid(MethodArgumentNotValidException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleItemNotAvailable(ItemNotAvailableException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleUserNotOwner(UserNotOwnerException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.FORBIDDEN.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUserHeaderNotFound(UserHeaderNotFoundException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handle(Exception e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArgumentNotValid(ConstraintViolationException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleStateNotValid(StateNotValidException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCommentException(CommentException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWrongBookingDatesException(WrongBookingDatesException e) {
        return ApiError.builder().error(e.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build();
    }
}

