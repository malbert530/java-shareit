package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles(profiles = {"test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingRepositoryTest {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private Item itemToSave;
    private User user;
    private User requester;
    private Booking booking;
    private final LocalDateTime created = LocalDateTime.of(2025, 1, 1, 1, 10, 0);
    private final LocalDateTime firstDate = LocalDateTime.of(2025, 1, 1, 10, 10, 0);
    private final LocalDateTime secondDate = LocalDateTime.of(2025, 1, 1, 11, 11, 0);

    @BeforeEach
    void setData() {
        String name = "Name";
        String email = "name@mail.com";
        user = new User();
        user.setName(name);
        user.setEmail(email);

        userRepository.save(user);

        requester = new User();
        requester.setName("Tom");
        requester.setEmail("tom@mail.com");

        userRepository.save(requester);

        ItemRequest request = new ItemRequest();
        request.setRequester(requester);
        request.setDescription("Need item");
        request.setCreated(created);

        requestRepository.save(request);

        itemToSave = new Item();
        String itemName = "Item";
        itemToSave.setName(itemName);
        itemToSave.setOwner(user);
        itemToSave.setAvailable(true);
        String description = "Description of Item";
        itemToSave.setDescription(description);
        itemToSave.setRequest(request);

        itemRepository.save(itemToSave);

        booking = new Booking();
        booking.setBooker(requester);
        booking.setItem(itemToSave);
        booking.setStatus(Status.WAITING);
        booking.setStart(firstDate);
        booking.setEnd(secondDate);

        bookingRepository.save(booking);
    }

    @Test
    void findByBookerId() {
        List<Booking> bookings = bookingRepository.findByBookerId(requester.getId());

        assertNotNull(bookings);
        assertThat(bookings.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(booking);
    }

    @Test
    void findAllByBookerIdAndStartAfterOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(requester.getId(), created);

        assertNotNull(bookings);
        assertThat(bookings.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(booking);
    }

    @Test
    void findAllByItemOwnerIdOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(user.getId());

        assertNotNull(bookings);
        assertThat(bookings.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(booking);
    }

    @Test
    void findAllByItemIdAndBookerIdAndEndBefore() {
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerIdAndEndBefore(itemToSave.getId(), requester.getId(), LocalDateTime.now());

        assertNotNull(bookings);
        assertThat(bookings.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(booking);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
        requestRepository.deleteAll();
    }
}
