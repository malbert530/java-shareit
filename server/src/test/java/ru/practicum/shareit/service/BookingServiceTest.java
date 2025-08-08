package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookingServiceTest {
    private BookingService service;
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final UserService userService = mock(UserService.class);
    private final ItemService itemService = mock(ItemService.class);
    private final Long ownerId = 1L;
    private final Long bookerId = 77L;
    private final Long bookingId = 23L;
    private final Long itemId = 2L;
    private final LocalDateTime created = LocalDateTime.of(2025, 1, 1, 1, 10, 0);
    private final LocalDateTime firstDate = LocalDateTime.of(2025, 1, 1, 10, 10, 0);
    private final LocalDateTime secondDate = LocalDateTime.of(2025, 1, 1, 11, 11, 0);
    private final String bookerName = "Name";
    private final String email = "name@mail.com";
    private final String ownerName = "Tom";
    private final String ownerEmail = "tom@mail.com";
    private final String itemName = "Item";
    private final String description = "Item Description";
    private final Boolean available = true;

    private BookingCreateDto bookingCreateDto;
    private Booking booking;
    private User booker;
    private User owner;
    private Item item;

    @BeforeEach
    void setData() {
        service = new BookingService(bookingRepository, userService, itemService);
        booker = new User(bookerId, bookerName, email);
        owner = new User(ownerId, ownerName, ownerEmail);

        ItemRequest request = new ItemRequest(44L, "Need item", created, booker);
        item = new Item(itemId, itemName, description, available, owner, request);

        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(firstDate);
        bookingCreateDto.setEnd(secondDate);

        booking = new Booking(bookingId, item, booker, firstDate, secondDate, Status.WAITING);
    }

    @Test
    void create() {
        when(userService.getUserIfExistOrElseThrow(bookerId)).thenReturn(booker);
        when(itemService.getItemIfExistOrElseThrow(itemId)).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto createdBooking = service.create(bookerId, bookingCreateDto);

        assertNotNull(createdBooking.getId());
        assertEquals(createdBooking.getItem().getName(), itemName);
        assertEquals(createdBooking.getBooker().getName(), bookerName);
    }

    @Test
    void create_itemNotAvailable_shouldThrowException() {
        when(userService.getUserIfExistOrElseThrow(bookerId)).thenReturn(booker);
        when(itemService.getItemIfExistOrElseThrow(itemId)).thenReturn(item);
        item.setAvailable(false);

        assertThrows(ItemNotAvailableException.class, () -> service.create(bookerId, bookingCreateDto));
    }

    @Test
    void approveByOwner() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto approvedByOwner = service.approveByOwner(ownerId, bookingId, true);

        assertEquals(Status.APPROVED, approvedByOwner.getStatus());
    }

    @Test
    void approveByOwner_reject() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto approvedByOwner = service.approveByOwner(ownerId, bookingId, false);

        assertEquals(Status.REJECTED, approvedByOwner.getStatus());
    }

    @Test
    void approveByOwner_alreadyApproved_shouldThrowException() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        booking.setStatus(Status.APPROVED);

        assertThrows(ValidationException.class, () -> service.approveByOwner(ownerId, bookingId, true));
    }

    @Test
    void approveByOwner_userNotOwner_shouldThrowException() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(UserNotOwnerException.class, () -> service.approveByOwner(bookerId, bookingId, true));
    }

    @Test
    void getBookingById() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto bookingById = service.getBookingById(bookingId);

        assertEquals(bookingById.getItem().getName(), itemName);
        assertEquals(bookingById.getBooker().getName(), bookerName);
    }

    @Test
    void getBookingById_notExist_shouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> service.getBookingById(888L));
    }

    @Test
    void getAllBookerBookings() {
        when(bookingRepository.findByBookerId(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> allBookerBookings = service.getAllBookerBookings(bookerId, State.ALL);

        assertEquals(itemName, allBookerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allBookerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllBookerBookings_stateCurrent() {
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> allBookerBookings = service.getAllBookerBookings(bookerId, State.CURRENT);

        assertEquals(itemName, allBookerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allBookerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllBookerBookings_statePast() {
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> allBookerBookings = service.getAllBookerBookings(bookerId, State.PAST);

        assertEquals(itemName, allBookerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allBookerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllBookerBookings_stateFuture() {
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> allBookerBookings = service.getAllBookerBookings(bookerId, State.FUTURE);

        assertEquals(itemName, allBookerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allBookerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllBookerBookings_stateRejected() {
        when(bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDesc(anyLong(), anyString())).thenReturn(List.of(booking));

        List<BookingResponseDto> allBookerBookings = service.getAllBookerBookings(bookerId, State.REJECTED);

        assertEquals(itemName, allBookerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allBookerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllBookerBookings_stateWaiting() {
        when(bookingRepository.findByBookerIdAndStatusEqualsOrderByStartDesc(anyLong(), anyString())).thenReturn(List.of(booking));

        List<BookingResponseDto> allBookerBookings = service.getAllBookerBookings(bookerId, State.WAITING);

        assertEquals(itemName, allBookerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allBookerBookings.getFirst().getBooker().getName());
    }


    @Test
    void getAllOwnerBookings() {
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> allOwnerBookings = service.getAllOwnerBookings(ownerId, State.ALL);

        assertEquals(itemName, allOwnerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allOwnerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllOwnerBookings_stateCurrent() {
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> allOwnerBookings = service.getAllOwnerBookings(ownerId, State.CURRENT);

        assertEquals(itemName, allOwnerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allOwnerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllOwnerBookings_statePast() {
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> allOwnerBookings = service.getAllOwnerBookings(bookerId, State.PAST);

        assertEquals(itemName, allOwnerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allOwnerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllOwnerBookings_stateFuture() {
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> allOwnerBookings = service.getAllOwnerBookings(bookerId, State.FUTURE);

        assertEquals(itemName, allOwnerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allOwnerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllOwnerBookings_stateRejected() {
        when(bookingRepository.findAllByItemOwnerIdAndStatusEqualsOrderByStartDesc(anyLong(), anyString())).thenReturn(List.of(booking));

        List<BookingResponseDto> allOwnerBookings = service.getAllOwnerBookings(bookerId, State.REJECTED);

        assertEquals(itemName, allOwnerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allOwnerBookings.getFirst().getBooker().getName());
    }

    @Test
    void getAllOwnerBookings_stateWaiting() {
        when(bookingRepository.findAllByItemOwnerIdAndStatusEqualsOrderByStartDesc(anyLong(), anyString())).thenReturn(List.of(booking));

        List<BookingResponseDto> allOwnerBookings = service.getAllOwnerBookings(bookerId, State.WAITING);

        assertEquals(itemName, allOwnerBookings.getFirst().getItem().getName());
        assertEquals(bookerName, allOwnerBookings.getFirst().getBooker().getName());
    }

}
