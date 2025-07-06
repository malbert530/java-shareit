package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.CommentException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public List<ItemWithDateDto> getAllItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не существует"));

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
        Item itemById = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не существует"));
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream().map(CommentMapper::toDto).toList();
        return ru.practicum.shareit.item.mapper.ItemMapper.itemToDtoWithDate(itemById, comments, null, null);
    }

    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не существует"));
        Item item = ru.practicum.shareit.item.mapper.ItemMapper.dtoToItem(itemDto);
        item.setOwner(owner);
        Item createdItem = itemRepository.save(item);
        return ru.practicum.shareit.item.mapper.ItemMapper.itemToDto(createdItem);
    }

    public ItemDto update(ItemDto item, Long itemId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не существует"));
        Item existItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не существует"));
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
        return ru.practicum.shareit.item.mapper.ItemMapper.itemToDto(updatedItem);
    }

    public List<ItemDto> getItemsBySearch(String text) {
        List<ItemDto> itemsDtoBySearch = new ArrayList<>();
        if (!text.isBlank()) {
            itemsDtoBySearch = itemRepository.findByAvailableTrueAndNameIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(text, text)
                    .stream()
                    .map(ru.practicum.shareit.item.mapper.ItemMapper::itemToDto)
                    .toList();
        }
        return itemsDtoBySearch;
    }

    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не существует"));
        Item existItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не существует"));
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now());
        if (bookings.isEmpty()) {
            String errorMessage = String.format("Пользователь с id = %d не арендовал вещь с itemId = %d или аренда еще не заверщилась", userId, itemId);
            log.warn(errorMessage);
            throw new CommentException(errorMessage);
        }
        Comment comment = CommentMapper.toComment(commentDto, existItem, user);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }
}
