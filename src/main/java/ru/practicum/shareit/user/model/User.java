package ru.practicum.shareit.user.model;

import lombok.Data;

@Data
public class User {
    Long id;
    String name;
    String email;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasEmail() {
        return !(email == null || email.isBlank());
    }
}
