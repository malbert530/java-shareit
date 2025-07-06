package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemWithDateDto> getAllItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен HTTP-запрос на получение всех вещей пользователя с id {}", userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithDateDto getItemById(@PathVariable Long itemId) {
        log.info("Получен HTTP-запрос на получение вещи по id {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearch(@RequestParam String text) {
        log.info("Получен HTTP-запрос на поиск вещи");
        return itemService.getItemsBySearch(text);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemDto itemDto) {
        log.info("Получен HTTP-запрос на создание вещи: {}", itemDto);
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto item, @PathVariable @Min(1) Long itemId) {
        log.info("Получен HTTP-запрос на обновление вещи c id = {} : {}", itemId, item);
        return itemService.update(item, itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody @Valid CommentDto commentDto) {
        log.info("Получен HTTP-запрос на создание комментария к вещи: {}", commentDto);
        return itemService.createComment(userId, itemId, commentDto);
    }
}
