package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    public BookingResponseDto getBookingById(Long bookingId) {
        Booking booking = getBookingIfExistOrElseThrow(bookingId);
        return BookingMapper.bookingToDto(booking);
    }

    private Booking getBookingIfExistOrElseThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с id " + bookingId + " не существует"));
    }

    public List<BookingResponseDto> getAllBookerBookings(Long userId, State state) {
        userService.getUserIfExistOrElseThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        if (state.equals(State.ALL)) {
            List<Booking> allBookings = bookingRepository.findByBookerId(userId);
            return allBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.CURRENT)) {
            List<Booking> currentBookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now);
            return currentBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.PAST)) {
            List<Booking> endedBookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            return endedBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.FUTURE)) {
            List<Booking> futureBookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            return futureBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.REJECTED)) {
            List<Booking> rejectedBookings = bookingRepository
                    .findByBookerIdAndStatusEqualsOrderByStartDesc(userId, Status.REJECTED.toString());
            return rejectedBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.WAITING)) {
            List<Booking> waitingBookings = bookingRepository
                    .findByBookerIdAndStatusEqualsOrderByStartDesc(userId, Status.WAITING.toString());
            return waitingBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else {
            throw new StateNotValidException("Wrong parameter " + state);
        }
    }

    public List<BookingResponseDto> getAllOwnerBookings(Long userId, State state) {
        userService.getUserIfExistOrElseThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        if (state.equals(State.ALL)) {
            List<Booking> allBookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            return allBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.CURRENT)) {
            List<Booking> currentBookings = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now);
            return currentBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.PAST)) {
            List<Booking> endedBookings = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            return endedBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.FUTURE)) {
            List<Booking> futureBookings = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            return futureBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.REJECTED)) {
            List<Booking> rejectedBookings = bookingRepository
                    .findAllByItemOwnerIdAndStatusEqualsOrderByStartDesc(userId, Status.REJECTED.toString());
            return rejectedBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else if (state.equals(State.WAITING)) {
            List<Booking> waitingBookings = bookingRepository
                    .findAllByItemOwnerIdAndStatusEqualsOrderByStartDesc(userId, Status.WAITING.toString());
            return waitingBookings.stream()
                    .map(BookingMapper::bookingToDto)
                    .toList();
        } else {
            throw new StateNotValidException("Wrong parameter " + state);
        }
    }

    public BookingResponseDto create(Long userId, BookingCreateDto bookingCreateDto) {
        User user = userService.getUserIfExistOrElseThrow(userId);
        if (!bookingCreateDto.getStart().isBefore(bookingCreateDto.getEnd())) {
            String errorMessage = String.format("Дата старта аренды %s должна быть раньше даты окончания %s",
                    bookingCreateDto.getStart().toString(), bookingCreateDto.getEnd().toString());
            throw new ValidationException(errorMessage);
        }
        Long itemId = bookingCreateDto.getItemId();
        Item item = itemService.getItemIfExistOrElseThrow(itemId);
        if (!item.getAvailable()) {
            String errorMessage = String.format("Вещь с id %d недоступна бля бронирования", itemId);
            throw new ItemNotAvailableException(errorMessage);
        }
        Booking booking = BookingMapper.dtoToBooking(bookingCreateDto, user, item);
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.bookingToDto(savedBooking);
    }

    public BookingResponseDto approveByOwner(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBookingIfExistOrElseThrow(bookingId);
        if (!booking.getStatus().equals(Status.WAITING)) {
            String errorMessage = String.format("Статус бронирования уже %s, подтверждение не требуется", booking.getStatus());
            throw new ValidationException(errorMessage);
        }
        Long itemId = booking.getItem().getId();
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            String errorMessage = String.format("Пользователь с id %d не является владельцем вещи с id %d", userId, itemId);
            throw new UserNotOwnerException(errorMessage);
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.bookingToDto(updatedBooking);
    }
}
