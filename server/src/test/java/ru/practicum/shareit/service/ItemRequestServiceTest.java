package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithResponses;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ItemRequestServiceTest {
    private final ItemRequestRepository itemRequestRepository = mock(ItemRequestRepository.class);
    private final UserService userService = mock(UserService.class);
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final Long userId = 1L;
    private final String description = "Description";
    private final Long itemRequestId = 34L;
    private final Long itemId = 2L;
    private final String itemName = "Item";
    private final LocalDateTime created = LocalDateTime.of(2025, 1, 1, 1, 10, 0);
    private User user;
    private User owner;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private ItemRequestService service;
    private Item item;

    @BeforeEach
    void setData() {
        service = new ItemRequestService(itemRequestRepository, userService, itemRepository);

        user = new User(userId, "Name", "name@mail.com");
        owner = new User(54L, "Tom", "tom@mail.com");

        itemRequest = new ItemRequest(userId, description, created, user);

        itemRequestDto = new ItemRequestDto(itemRequestId, description, created, userId);

        item = new Item(itemId, itemName, description, true, owner, itemRequest);
    }

    @Test
    void create() {
        when(userService.getUserIfExistOrElseThrow(userId)).thenReturn(user);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto createdRequest = service.create(userId, itemRequestDto);

        assertNotNull(createdRequest.getId());
        assertEquals(createdRequest.getDescription(), description);
        assertEquals(createdRequest.getRequesterId(), userId);
    }

    @Test
    void getRequestById() {
        when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(itemRequestId)).thenReturn(List.of(item));

        ItemRequestDtoWithResponses requestById = service.getRequestById(itemRequestId);

        assertEquals(requestById.getDescription(), description);
        assertEquals(requestById.getItems().getFirst().getOwnerId(), owner.getId());
    }

    @Test
    void getRequestById_notExist_shouldThrowException() {
        when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.empty());

        assertThrows(ItemRequestNotFoundException.class, () -> service.getRequestById(itemRequestId));
        verify(itemRequestRepository).findById(itemRequestId);
    }

    @Test
    void getAllRequests() {
        when(itemRequestRepository.findAllByOrderByCreatedDesc()).thenReturn(List.of(itemRequest));

        List<ItemRequestDto> allRequests = service.getAllRequests(owner.getId());

        assertNotNull(allRequests);
        assertEquals(allRequests.getFirst().getRequesterId(), userId);
        assertEquals(allRequests.getFirst().getDescription(), description);
    }

    @Test
    void getAllUserRequests() {
        when(userService.getUserIfExistOrElseThrow(userId)).thenReturn(user);
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId)).thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdNotNull()).thenReturn(List.of(item));

        List<ItemRequestDtoWithResponses> allUserRequests = service.getAllUserRequests(userId);

        assertNotNull(allUserRequests);
        assertEquals(allUserRequests.getFirst().getDescription(), description);
        assertEquals(allUserRequests.getFirst().getItems().getFirst().getName(), itemName);
        assertEquals(allUserRequests.getFirst().getItems().getFirst().getOwnerId(), owner.getId());
    }

}
