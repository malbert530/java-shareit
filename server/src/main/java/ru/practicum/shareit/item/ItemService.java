package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.CommentException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;

    public List<ItemWithDateDto> getAllItems(Long userId) {
        User user = userService.getUserIfExistOrElseThrow(userId);

        Map<Item, List<Booking>> bookedItems = bookingRepository
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now())
                .stream()
                .collect(Collectors.groupingBy(Booking::getItem));
        List<Item> allItems = itemRepository.findByOwner(user);


        Map<Item, Booking> next = new HashMap<>();
        Map<Item, Booking> last = new HashMap<>();

        for (Item item : bookedItems.keySet()) {
            Booking nextBooking = bookedItems.get(item).stream()
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);
            Booking lastBooking = bookedItems.get(item).stream()
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);
            next.put(item, nextBooking);
            last.put(item, lastBooking);
        }

        List<Comment> allItemsComments = commentRepository.findByItemIn(allItems);
        Map<Item, List<Comment>> itemsCommentsMap = allItemsComments.stream()
                .collect(Collectors.groupingBy(Comment::getItem));
        return allItems.stream()
                .map(i -> ItemMapper.itemToDtoWithDate(i,
                        CommentMapper.toListDto(itemsCommentsMap.getOrDefault(i, Collections.emptyList())),
                        next.get(i), last.get(i)))
                .toList();
    }

    public ItemWithDateDto getItemById(Long itemId) {
        Item itemById = getItemIfExistOrElseThrow(itemId);
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream().map(CommentMapper::toDto).toList();
        return ItemMapper.itemToDtoWithDate(itemById, comments, null, null);
    }

    public Item getItemIfExistOrElseThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не существует"));
    }

    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userService.getUserIfExistOrElseThrow(userId);
        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new ItemRequestNotFoundException("Запроса с id " + itemDto.getRequestId() + " не существует"));
        }
        Item item = ItemMapper.dtoToItem(itemDto);
        item.setOwner(owner);
        item.setRequest(request);
        Item createdItem = itemRepository.save(item);
        return ItemMapper.itemToDto(createdItem);
    }

    public ItemDto update(ItemDto item, Long itemId, Long userId) {
        userService.getUserIfExistOrElseThrow(userId);
        Item existItem = getItemIfExistOrElseThrow(itemId);
        if (!existItem.getOwner().getId().equals(userId)) {
            String errorMessage = String.format("Пользователь с id = %d не является владельцем вещи с itemId = %d", userId, itemId);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
        if (item.getName() != null) {
            existItem.setName(item.getName());
        }
        if (item.getAvailable() != null) {
            existItem.setAvailable(item.getAvailable());
        }
        if (item.getDescription() != null) {
            existItem.setDescription(item.getDescription());
        }
        Item updatedItem = itemRepository.save(existItem);
        return ItemMapper.itemToDto(updatedItem);
    }

    public List<ItemDto> getItemsBySearch(String text) {
        return itemRepository.findByAvailableTrueAndNameIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(text, text)
                .stream()
                .map(ItemMapper::itemToDto)
                .toList();
    }

    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userService.getUserIfExistOrElseThrow(userId);
        Item existItem = getItemIfExistOrElseThrow(itemId);
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now());
        if (bookings.isEmpty()) {
            String errorMessage = String.format("Пользователь с id = %d не арендовал вещь с itemId = %d или аренда еще не завершилась", userId, itemId);
            log.warn(errorMessage);
            throw new CommentException(errorMessage);
        }
        Comment comment = CommentMapper.toComment(commentDto, existItem, user);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }
}
