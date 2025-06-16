package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ItemRepository {
    private final HashMap<Long, HashMap<Long, Item>> items = new HashMap<>();
    private Long itemCount = 0L;

    public List<Item> findAllUserItems(Long userId) {
        return items.get(userId).values().stream().toList();
    }

    public Item findItemById(Long itemId) {
        Optional<HashMap<Long, Item>> userItems = items.values().stream()
                .filter(o1 -> o1.containsKey(itemId))
                .findFirst();
        if (userItems.isPresent()) {
            return userItems.get().get(itemId);
        }
        String errorMessage = String.format("Вещь с id %d не найдена", itemId);
        log.warn(errorMessage);
        throw new ItemNotFoundException(errorMessage);
    }

    public Item create(Item item, Long userId) {
        item.setId(++itemCount);
        if (!items.containsKey(userId)) {
            HashMap<Long, Item> userItems = new HashMap<>();
            userItems.put(item.getId(), item);
            items.put(userId, userItems);
        } else {
            items.get(userId).put(item.getId(), item);
        }
        return item;
    }

    public Item update(Item item, Long itemId, Long userId) {
        Item oldItem = items.get(userId).get(itemId);
        if (item.hasName()) {
            oldItem.setName(item.getName());
        }
        if (item.hasDescription()) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.hasAvailable()) {
            oldItem.setAvailable(item.getAvailable());
        }
        log.info("Обновлена вещь с id = {}", itemId);
        return oldItem;
    }

    public List<Item> getItemsBySearch(String text) {
        List<Item> itemsBySearch = new ArrayList<>();
        for (HashMap<Long, Item> entry : items.values()) {
            List<Item> list = entry.values().stream()
                    .filter(Item::getAvailable)
                    .filter(o -> o.getName().toLowerCase().contains(text) || o.getDescription().toLowerCase().contains(text))
                    .toList();
            itemsBySearch.addAll(list);
        }
        return itemsBySearch;
    }
}
