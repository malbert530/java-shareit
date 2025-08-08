package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.exception.WrongBookingDatesException;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getAllBookerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(name = "state", defaultValue = "all") String stateParam) {
        State state = State.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, bookerId={}", stateParam, userId);
        return bookingClient.getAllBookerBookings(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(name = "state", defaultValue = "all") String stateParam) {
        State state = State.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, ownerId={}", stateParam, userId);
        return bookingClient.getAllOwnerBookings(userId, state);
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestBody @Valid BookingCreateDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        if (!requestDto.getStart().isBefore(requestDto.getEnd())) {
            String errorMessage = String.format("Дата старта аренды %s должна быть раньше даты окончания %s",
                    requestDto.getStart(), requestDto.getEnd());
            log.warn(errorMessage);
            throw new WrongBookingDatesException(errorMessage);
        }
        return bookingClient.createBooking(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveByOwner(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam boolean approved, @PathVariable Long bookingId) {
        log.info("Get approve booking {}, ownerId={}, approved={}", bookingId, userId, approved);
        return bookingClient.approveByOwner(userId, bookingId, approved);
    }
}