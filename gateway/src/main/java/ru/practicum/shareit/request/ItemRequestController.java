package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping()
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Creating request");
        return itemRequestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get all requests");
        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get all requests by userId {}", userId);
        return itemRequestClient.getAllUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        log.info("Get request by id {}", requestId);
        return itemRequestClient.getRequestById(requestId, userId);
    }
}