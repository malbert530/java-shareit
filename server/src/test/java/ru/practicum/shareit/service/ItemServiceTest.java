package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.CommentException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ItemServiceTest {
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final UserService userService = mock(UserService.class);
    private final ItemRequestRepository itemRequestRepository = mock(ItemRequestRepository.class);
    private ItemService service;
    private final String itemName = "Item";
    private final String description = "Description";
    private final Long requestId = 22L;
    private final Long userId = 1L;
    private final Long requesterId = 1L;
    private final Long itemId = 2L;
    private final String text = "Text";
    private final Boolean available = true;
    private final LocalDateTime created = LocalDateTime.of(2025, 1, 1, 1, 10, 0);
    private final LocalDateTime next = LocalDateTime.of(2025, 1, 1, 10, 10, 0);
    private final LocalDateTime last = LocalDateTime.of(2025, 1, 1, 11, 11, 0);
    private Item response;
    private ItemDto itemDto;
    private User owner;
    private ItemRequest itemRequest;
    private CommentDto commentDto;
    private Comment comment;
    private Booking booking;
    private User requester;


    @BeforeEach
    void setData() {
        service = new ItemService(itemRepository, bookingRepository, commentRepository, userService, itemRequestRepository);

        itemDto = new ItemDto();
        itemDto.setName(itemName);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);

        owner = new User(userId, "Name", "name@mail.com");
        requester = new User(requesterId, "Max", "max@mail.com");

        itemRequest = new ItemRequest(requestId, description, created, requester);

        response = new Item(itemId, itemName, description, available, owner, itemRequest);

        commentDto = new CommentDto();
        Long commentId = 123L;
        commentDto.setId(commentId);
        commentDto.setAuthorName(requester.getName());
        commentDto.setText(text);

        comment = new Comment(commentId, text, response, requester);

        Long bookingId = 77L;
        booking = new Booking(bookingId, response, requester, next, last, Status.WAITING);
    }

    @Test
    void create() {
        when(itemRepository.save(any(Item.class))).thenReturn(response);
        when(userService.getUserIfExistOrElseThrow(userId)).thenReturn(owner);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        ItemDto createdItem = service.create(userId, itemDto);

        assertNotNull(createdItem.getId());
        assertEquals(createdItem.getName(), itemName);
        assertEquals(createdItem.getDescription(), description);
    }

    @Test
    void create_requestNotExist_shouldThrowException() {
        when(userService.getUserIfExistOrElseThrow(userId)).thenReturn(owner);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(ItemRequestNotFoundException.class, () -> service.create(userId, itemDto));
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void update() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(response));
        when(itemRepository.save(any(Item.class))).thenReturn(response);

        ItemDto updatedItem = service.update(itemDto, itemId, userId);

        assertEquals(updatedItem.getName(), itemName);
        assertEquals(updatedItem.getDescription(), description);
        assertEquals(updatedItem.getAvailable(), available);
    }

    @Test
    void update_userNotOwner_shouldThrowException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(response));

        assertThrows(UserNotFoundException.class, () -> service.update(itemDto, itemId, 555L));
        verify(itemRepository).findById(itemId);
    }

    @Test
    void getItemById() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(response));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemWithDateDto itemById = service.getItemById(itemId);

        assertEquals(itemById.getName(), itemName);
        assertEquals(itemById.getDescription(), description);
        assertEquals(itemById.getAvailable(), available);
        assertEquals(itemById.getComments().getFirst().getText(), text);
        assertNull(itemById.getLastBooking());
    }

    @Test
    void getItemById_itemNotExist_shouldThrowException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> service.update(itemDto, itemId, userId));
        verify(itemRepository).findById(itemId);
    }

    @Test
    void getAllItems() {
        when(userService.getUserIfExistOrElseThrow(userId)).thenReturn(owner);
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(itemRepository.findByOwner(owner)).thenReturn(List.of(response));
        when(commentRepository.findByItemIn(List.of(response))).thenReturn(List.of(comment));

        List<ItemWithDateDto> allItems = service.getAllItems(userId);

        assertEquals(allItems.getFirst().getName(), itemName);
        assertEquals(allItems.getFirst().getDescription(), description);
        assertEquals(allItems.getFirst().getAvailable(), available);
        assertEquals(allItems.getFirst().getComments().getFirst().getText(), text);
        assertEquals(allItems.getFirst().getNextBooking(), next);
        assertEquals(allItems.getFirst().getLastBooking(), last);
    }

    @Test
    void getItemsBySearch() {
        when(itemRepository.findByAvailableTrueAndNameIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(text, text))
                .thenReturn(List.of(response));

        List<ItemDto> itemsBySearch = service.getItemsBySearch(text);

        assertNotNull(itemsBySearch);
        assertEquals(itemsBySearch.getFirst().getName(), itemName);
        assertEquals(itemsBySearch.getFirst().getDescription(), description);
        assertEquals(itemsBySearch.getFirst().getAvailable(), available);
    }

    @Test
    void createComment() {
        when(userService.getUserIfExistOrElseThrow(requesterId)).thenReturn(requester);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(response));
        when(bookingRepository.findAllByItemIdAndBookerIdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto createdComment = service.createComment(requesterId, itemId, commentDto);

        assertNotNull(createdComment.getId());
        assertEquals(createdComment.getText(), text);
        assertEquals(createdComment.getAuthorName(), requester.getName());
    }

    @Test
    void createComment_userNotBooking_shouldThrowException() {
        when(userService.getUserIfExistOrElseThrow(requesterId)).thenReturn(requester);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(response));
        when(bookingRepository.findAllByItemIdAndBookerIdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());


        assertThrows(CommentException.class, () -> service.createComment(userId, itemId, commentDto));
        verify(bookingRepository).findAllByItemIdAndBookerIdAndEndBefore(anyLong(), anyLong(), any(LocalDateTime.class));
    }
}
