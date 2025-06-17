package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;

    public List<ItemDto> getAllItems(Long userId) {
        userRepository.findById(userId);
        return itemRepository.findAllUserItems(userId).stream()
                .map(itemMapper::itemToDto)
                .toList();
    }

    public ItemDto getItemById(Long itemId) {
        Item itemById = itemRepository.findItemById(itemId);
        return itemMapper.itemToDto(itemById);
    }

    public ItemDto create(Long userId, ItemDto itemDto) {
        userRepository.findById(userId);
        Item item = itemMapper.dtoToItem(itemDto);
        item.setOwnerId(userId);
        Item createdItem = itemRepository.create(item);
        return itemMapper.itemToDto(createdItem);
    }

    public ItemDto update(ItemDto item, Long itemId, Long userId) {
        userRepository.findById(userId);
        Item existItem = itemRepository.findItemById(itemId);
        if (!existItem.getOwnerId().equals(userId)) {
            String errorMessage = String.format("Пользователь с id = %d не является владельцем вещи с itemId = %d", userId, itemId);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
        Item itemToUpdate = itemMapper.dtoToItem(item);
        Item updatedItem = itemRepository.update(itemToUpdate, itemId);
        return itemMapper.itemToDto(updatedItem);
    }

    public List<ItemDto> getItemsBySearch(String text) {
        List<ItemDto> itemsDtoBySearch = new ArrayList<>();
        if (!text.isBlank()) {
            itemsDtoBySearch = itemRepository.getItemsBySearch(text.toLowerCase()).stream()
                    .map(itemMapper::itemToDto)
                    .toList();
        }
        return itemsDtoBySearch;
    }
}
