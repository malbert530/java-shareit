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
        List<Item> allUserItems = itemRepository.findAllUserItems(userId);
        List<ItemDto> allUserItemsDto = new ArrayList<>();
        for (Item item : allUserItems) {
            allUserItemsDto.add(itemMapper.itemToDto(item));
        }
        return allUserItemsDto;
    }

    public ItemDto getItemById(Long itemId) {
        Item itemById = itemRepository.findItemById(itemId);
        return itemMapper.itemToDto(itemById);
    }

    public ItemDto create(Long userId, ItemDto itemDto) {
        userRepository.findById(userId);
        Item item = itemMapper.dtoToItem(itemDto);
        item.setOwnerId(userId);
        Item createdItem = itemRepository.create(item, userId);
        return itemMapper.itemToDto(createdItem);
    }

    public ItemDto update(Item item, Long itemId, Long userId) {
        userRepository.findById(userId);
        Item existItem = itemRepository.findItemById(itemId);
        if (!existItem.getOwnerId().equals(userId)) {
            String errorMessage = String.format("Пользователь с id = %d не является владельцем вещи с itemId = %d", userId, itemId);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
        Item updatedItem = itemRepository.update(item, itemId, userId);
        return itemMapper.itemToDto(updatedItem);
    }

    public List<ItemDto> getItemsBySearch(String text) {
        List<ItemDto> itemsDtoBySearch = new ArrayList<>();
        if (!text.isBlank()) {
            List<Item> itemsBySearch = itemRepository.getItemsBySearch(text.toLowerCase());
            for (Item bySearch : itemsBySearch) {
                ItemDto itemDto = itemMapper.itemToDto(bySearch);
                itemsDtoBySearch.add(itemDto);
            }
        }
        return itemsDtoBySearch;
    }
}
