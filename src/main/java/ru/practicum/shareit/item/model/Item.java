package ru.practicum.shareit.item.model;

import lombok.Data;

@Data
public class Item {
    Long id;
    String name;
    String description;
    Boolean available;
    Long ownerId;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasAvailable() {
        return !(available == null);
    }
}
