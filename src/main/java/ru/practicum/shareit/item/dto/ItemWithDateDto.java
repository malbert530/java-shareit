package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemWithDateDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private List<CommentDto> comments;

    private LocalDateTime nextBooking;

    private LocalDateTime lastBooking;
}
