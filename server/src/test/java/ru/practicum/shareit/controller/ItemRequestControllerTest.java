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
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithResponses;
import ru.practicum.shareit.request.dto.ItemResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestControllerTest {
    @MockBean
    ItemRequestService service;
    private final ObjectMapper mapper;
    private final MockMvc mockMvc;
    private ItemRequestDto itemRequestToCreate;
    private ItemRequestDto response;
    private ItemRequestDtoWithResponses dtoWithResponses;
    private final Long itemId = 1L;
    private final Long ownerId = 3L;
    private final Long requestId = 22L;
    private final Long userId = 3L;
    private final String description = "Description";
    private final LocalDateTime created = LocalDateTime.of(2025, 1, 1, 10, 10, 0);

    @BeforeEach
    void setData() {
        itemRequestToCreate = new ItemRequestDto();
        itemRequestToCreate.setDescription(description);

        response = new ItemRequestDto();
        response.setId(requestId);
        response.setRequesterId(userId);
        response.setDescription(description);
        response.setCreated(created);

        ItemResponseDto itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(itemId);
        itemResponseDto.setName("Item name");
        itemResponseDto.setOwnerId(ownerId);

        dtoWithResponses = new ItemRequestDtoWithResponses();
        dtoWithResponses.setId(requestId);
        dtoWithResponses.setCreated(created);
        dtoWithResponses.setDescription(description);
        dtoWithResponses.setItems(List.of(itemResponseDto));
    }

    @Test
    @SneakyThrows
    void createRequestTest() {
        when(service.create(userId, itemRequestToCreate)).thenReturn(response);

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                post("/requests")
                                        .content(mapper.writeValueAsString(itemRequestToCreate))
                                        .characterEncoding(StandardCharsets.UTF_8)
                                        .header("X-Sharer-User-Id", userId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        ItemRequestDto actualRequest = mapper.readValue(responseBody, ItemRequestDto.class);

        assertNotNull(actualRequest.getId());
        assertEquals(actualRequest.getRequesterId(), userId);
        assertEquals(actualRequest.getDescription(), description);
    }

    @Test
    @SneakyThrows
    void getAllRequestsTest() {

        when(service.getAllRequests()).thenReturn(List.of(response));

        mockMvc
                .perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value(description));
    }

    @Test
    @SneakyThrows
    void getAllUserRequestsTest() {

        when(service.getAllUserRequests(userId)).thenReturn(List.of(dtoWithResponses));

        mockMvc
                .perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value(description))
                .andExpect(jsonPath("$[0].items[0].id").value(itemId))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(ownerId));
    }

    @Test
    @SneakyThrows
    void getRequestByIdTest() {

        when(service.getRequestById(requestId)).thenReturn(dtoWithResponses);

        mockMvc
                .perform(get("/requests/" + requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.items[0].id").value(itemId))
                .andExpect(jsonPath("$.items[0].ownerId").value(ownerId));
    }
}
