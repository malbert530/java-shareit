package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.apiError.ApiError;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.handler.GlobalExceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ApiError apiError = handler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("User not found");
    }

    @Test
    void handleItemNotFound() {
        ItemNotFoundException ex = new ItemNotFoundException("Item not found");
        ApiError apiError = handler.handleItemNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("Item not found");
    }

    @Test
    void handleValidation() {
        ValidationException ex = new ValidationException("Validation error");
        ApiError apiError = handler.handleValidation(ex);

        assertEquals(HttpStatus.CONFLICT.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("Validation error");
    }

    @Test
    void handleItemNotAvailable() {
        ItemNotAvailableException ex = new ItemNotAvailableException("Item not available");
        ApiError apiError = handler.handleItemNotAvailable(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("Item not available");
    }

    @Test
    void handleUserNotOwner() {
        UserNotOwnerException ex = new UserNotOwnerException("User not owner");
        ApiError apiError = handler.handleUserNotOwner(ex);

        assertEquals(HttpStatus.FORBIDDEN.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("User not owner");
    }

    @Test
    void handleWrongBookingDatesException() {
        WrongBookingDatesException ex = new WrongBookingDatesException("Wrong booking dates");
        ApiError apiError = handler.handleWrongBookingDatesException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("Wrong booking dates");
    }

    @Test
    void handle() {
        Exception ex = new Exception("Unknown error");
        ApiError apiError = handler.handle(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("Unknown error");
    }

    @Test
    void handleCommentException() {
        CommentException ex = new CommentException("Wrong comment");
        ApiError apiError = handler.handleCommentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), apiError.getErrorCode());
        assertThat(apiError.getError()).isEqualTo("Wrong comment");
    }
}
