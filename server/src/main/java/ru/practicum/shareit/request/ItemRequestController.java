package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithResponses;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping()
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Получен HTTP-запрос на создание запроса");
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен HTTP-запрос на получение списка запросов всех пользователей");
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping()
    public List<ItemRequestDtoWithResponses> getAllUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен HTTP-запрос на получение списка запросов пользователя с id {}", userId);
        return itemRequestService.getAllUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoWithResponses getRequestById(@PathVariable Long requestId) {
        log.info("Получен HTTP-запрос на получение запроса по id {}", requestId);
        return itemRequestService.getRequestById(requestId);
    }
}
