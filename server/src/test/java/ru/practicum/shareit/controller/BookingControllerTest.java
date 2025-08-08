package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    @MockBean
    BookingService bookingService;
    private final ObjectMapper mapper;
    private final MockMvc mockMvc;
    private UserDto booker;
    private BookingCreateDto bookingCreateDto;
    private BookingResponseDto response;
    private final Long userId = 3L;
    private final Long itemId = 1L;
    private final Long bookingId = 15L;
    private final LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 10, 0);
    private final LocalDateTime end = LocalDateTime.of(2025, 1, 2, 10, 20, 0);

    @BeforeEach
    void setData() {
        booker = new UserDto();
        booker.setName("Name");
        booker.setEmail("name@mail.com");
        booker.setId(userId);

        ItemDto itemDto = new ItemDto();
        itemDto.setId(itemId);
        itemDto.setName("Item name");
        itemDto.setAvailable(true);
        itemDto.setDescription("Item description");
        itemDto.setRequestId(2L);

        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(itemId);
        bookingCreateDto.setStart(start);
        bookingCreateDto.setEnd(end);

        response = new BookingResponseDto();
        response.setId(bookingId);
        response.setItem(itemDto);
        response.setBooker(booker);
        response.setStart(start);
        response.setEnd(end);
        response.setStatus(Status.APPROVED);
    }

    @Test
    @SneakyThrows
    void createBookingTest() {
        when(bookingService.create(userId, bookingCreateDto)).thenReturn(response);

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                post("/bookings")
                                        .content(mapper.writeValueAsString(bookingCreateDto))
                                        .characterEncoding(StandardCharsets.UTF_8)
                                        .header("X-Sharer-User-Id", userId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        BookingResponseDto actualBooking = mapper.readValue(responseBody, BookingResponseDto.class);

        assertNotNull(actualBooking.getId());
        assertThat(actualBooking).usingRecursiveComparison().isEqualTo(response);
        assertEquals(actualBooking.getItem().getId(), itemId);
        assertEquals(actualBooking.getStart(), start);
    }

    @Test
    @SneakyThrows
    void approveByOwnerTest() {
        when(bookingService.approveByOwner(userId, bookingId, true)).thenReturn(response);

        mockMvc
                .perform(
                        patch("/bookings/" + bookingId)
                                .header("X-Sharer-User-Id", userId)
                                .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @SneakyThrows
    void getAllOwnerBookingsTest() {
        booker.setId(33L);
        when(bookingService.getAllOwnerBookings(userId, State.ALL)).thenReturn(List.of(response));

        mockMvc
                .perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId))
                .andExpect(jsonPath("$[0].booker.id").value(33L));
    }

    @Test
    @SneakyThrows
    void getAllBookerBookingsTest() {

        when(bookingService.getAllBookerBookings(userId, State.ALL)).thenReturn(List.of(response));

        mockMvc
                .perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId))
                .andExpect(jsonPath("$[0].booker.id").value(userId));
    }

    @Test
    @SneakyThrows
    void getBookingByIdTest() {

        when(bookingService.getBookingById(bookingId)).thenReturn(response);

        MvcResult mvcResult =
                mockMvc
                        .perform(get("/bookings/" + bookingId))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        BookingResponseDto actualBooking = mapper.readValue(responseBody, BookingResponseDto.class);

        assertThat(actualBooking).usingRecursiveComparison().isEqualTo(response);
    }
}
