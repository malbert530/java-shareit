package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId) {
        log.info("Получен HTTP-запрос на получение бронирования по id {}", bookingId);
        return bookingService.getBookingById(bookingId);
    }

    @GetMapping()
    public List<BookingResponseDto> getAllBookerBookings(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam(defaultValue = "ALL") State state) {
        log.info("Получен HTTP-запрос на получение списка бронирований со статусом {} пользователя с id {}", state.toString(), userId);
        return bookingService.getAllBookerBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam(defaultValue = "ALL") State state) {
        log.info("Получен HTTP-запрос на получение списка бронирований со статусом {} владельца с id {}", state.toString(), userId);
        return bookingService.getAllOwnerBookings(userId, state);
    }

    @PostMapping()
    public BookingResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody BookingCreateDto bookingCreateDto) {
        log.info("Получен HTTP-запрос на создание бронирования");
        return bookingService.create(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveByOwner(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam boolean approved, @PathVariable Long bookingId) {
        log.info("Получен HTTP-запрос на изменение статуса бронирования c id {} от владельца c id {} на {}", bookingId, userId, approved);
        return bookingService.approveByOwner(userId, bookingId, approved);
    }

}
