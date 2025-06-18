package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ItemRepository {
    private final HashMap<Long, Item> items = new HashMap<>();

    public List<Item> findAllUserItems(Long userId) {
        return items.values().stream().filter(o -> o.getOwnerId().equals(userId)).toList();
    }

    public Item findItemById(Long itemId) {
        if (items.containsKey(itemId)) {
            return items.get(itemId);
        }
        String errorMessage = String.format("Вещь с id %d не найдена", itemId);
        log.warn(errorMessage);
        throw new ItemNotFoundException(errorMessage);
    }

    public Item create(Item item) {
        Long id = getId();
        item.setId(id);
        items.put(id, item);
        return item;
    }

    public Item update(Item item, Long itemId) {
        Item oldItem = items.get(itemId);
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
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(o -> o.getName().toLowerCase().contains(text) || o.getDescription().toLowerCase().contains(text))
                .toList();
    }

    private Long getId() {
        Long id = 0L;
        if (!items.isEmpty()) {
            id = items.keySet().stream().max(Long::compareTo).get();
        }
        return ++id;
    }
}
