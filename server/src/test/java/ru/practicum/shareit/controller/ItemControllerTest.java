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
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {
    @MockBean
    ItemService itemService;
    private final ObjectMapper mapper;
    private final MockMvc mockMvc;
    private ItemDto itemDto;
    private final Long userId = 3L;
    private final Long itemId = 1L;
    private final String name = "Item name";
    private final String description = "Item description";
    private final Boolean available = true;
    private final Long requestId = 2L;
    private final List<CommentDto> comments = new ArrayList<>();
    private final LocalDateTime nextBooking = LocalDateTime.of(2025, 1, 1, 10, 10, 0);
    private final LocalDateTime lastBooking = LocalDateTime.of(2025, 1, 2, 20, 20, 0);

    @BeforeEach
    void setData() {
        itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setAvailable(available);
        itemDto.setDescription(description);
        itemDto.setRequestId(requestId);
    }

    @Test
    @SneakyThrows
    void createItemTest() {
        ItemDto savedItem = new ItemDto();
        savedItem.setId(itemId);
        savedItem.setName(name);
        savedItem.setDescription(description);
        savedItem.setAvailable(available);
        savedItem.setRequestId(requestId);

        when(itemService.create(userId, itemDto)).thenReturn(savedItem);

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                post("/items")
                                        .content(mapper.writeValueAsString(itemDto))
                                        .characterEncoding(StandardCharsets.UTF_8)
                                        .header("X-Sharer-User-Id", userId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        ItemDto actualItemDto = mapper.readValue(responseBody, ItemDto.class);

        assertNotNull(actualItemDto.getId());
        assertThat(actualItemDto).usingRecursiveComparison().ignoringFields("id").isEqualTo(itemDto);
    }

    @Test
    @SneakyThrows
    void updateItemTest() {
        ItemDto updatedItem = new ItemDto();
        updatedItem.setId(itemId);
        updatedItem.setName(name);
        updatedItem.setDescription(description);
        updatedItem.setAvailable(available);
        updatedItem.setRequestId(requestId);

        when(itemService.update(itemDto, itemId, userId)).thenReturn(updatedItem);

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                patch("/items/" + itemId)
                                        .content(mapper.writeValueAsString(itemDto))
                                        .characterEncoding(StandardCharsets.UTF_8)
                                        .header("X-Sharer-User-Id", userId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        ItemDto actualItem = mapper.readValue(responseBody, ItemDto.class);

        assertEquals(actualItem.getId(), itemId);
        assertThat(actualItem).usingRecursiveComparison().ignoringFields("id").isEqualTo(itemDto);
    }

    @Test
    @SneakyThrows
    void getItemByIdTest() {
        ItemWithDateDto item = new ItemWithDateDto();
        item.setId(itemId);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setComments(comments);
        item.setNextBooking(nextBooking);
        item.setLastBooking(lastBooking);

        when(itemService.getItemById(itemId)).thenReturn(item);

        MvcResult mvcResult =
                mockMvc
                        .perform(get("/items/" + itemId))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        ItemWithDateDto actualItem = mapper.readValue(responseBody, ItemWithDateDto.class);

        assertThat(actualItem).usingRecursiveComparison().isEqualTo(item);
    }

    @Test
    @SneakyThrows
    void getAllItemsTest() {
        ItemWithDateDto item = new ItemWithDateDto();
        item.setId(itemId);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setComments(comments);
        item.setNextBooking(nextBooking);
        item.setLastBooking(lastBooking);

        when(itemService.getAllItems(userId)).thenReturn(List.of(item));

        mockMvc
                .perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].name").value(name));
    }

    @Test
    @SneakyThrows
    void getItemsBySearchTest() {
        String text = "item";
        ItemDto item = new ItemDto();
        item.setId(itemId);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setRequestId(requestId);

        when(itemService.getItemsBySearch(text)).thenReturn(List.of(item));

        mockMvc
                .perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].name").value(name));
    }

    @Test
    @SneakyThrows
    void createCommentTest() {
        CommentDto commentToSave = new CommentDto();
        commentToSave.setText("Comment text");
        commentToSave.setAuthorName("Name");

        CommentDto savedComment = new CommentDto();
        savedComment.setId(1L);
        savedComment.setText("Comment text");
        savedComment.setCreated(LocalDateTime.of(2025, 1, 1, 11, 0, 0));
        savedComment.setAuthorName("Name");

        when(itemService.createComment(userId, itemId, commentToSave)).thenReturn(savedComment);

        MvcResult mvcResult =
                mockMvc
                        .perform(
                                post("/items/" + itemId + "/comment")
                                        .content(mapper.writeValueAsString(commentToSave))
                                        .characterEncoding(StandardCharsets.UTF_8)
                                        .header("X-Sharer-User-Id", userId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        CommentDto actualComment = mapper.readValue(responseBody, CommentDto.class);

        assertNotNull(actualComment.getId());
        assertThat(actualComment).usingRecursiveComparison().ignoringFields("id", "created").isEqualTo(commentToSave);
    }
}