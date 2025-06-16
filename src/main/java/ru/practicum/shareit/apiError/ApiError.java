package ru.practicum.shareit.apiError;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
    private String error;
    private Integer errorCode;
}
