package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.UserHeaderNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final HashMap<Long, User> users = new HashMap<>();


    public User findById(Long id) {
        if (id == null) {
            String errorMessage = "Не указан id владельца вещи";
            log.warn(errorMessage);
            throw new UserHeaderNotFoundException(errorMessage);
        } else if (users.containsKey(id)) {
            return users.get(id);
        } else {
            String errorMessage = String.format("Пользователь с id %d не найден", id);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }
    }

    public User create(User newUser) {
        Long id = getId();
        newUser.setId(id);
        users.put(id, newUser);
        log.info("Создан новый пользователь с id = {}", id);
        return newUser;
    }

    public User update(User newFieldsUser, Long id) {
        User oldUser = users.get(id);
        if (newFieldsUser.hasEmail()) {
            checkEmailExist(newFieldsUser.getEmail());
            oldUser.setEmail(newFieldsUser.getEmail());
        }
        if (newFieldsUser.hasName()) {
            oldUser.setName(newFieldsUser.getName());
        }
        log.info("Обновлён пользователь с id = {}", id);
        return oldUser;
    }

    public void delete(Long id) {
        users.remove(id);
        log.info("Удален пользователь с id = {}", id);
    }

    private Long getId() {
        Long id = 0L;
        if (!users.isEmpty()) {
            id = users.keySet().stream().max(Long::compareTo).get();
        }
        return ++id;
    }

    public void checkEmailExist(String email) {
        Optional<User> first = users.values().stream().filter(o1 -> o1.getEmail().equals(email)).findFirst();
        if (first.isPresent()) {
            String errorMessage = String.format("Пользователь с email %s уже существует", email);
            log.warn(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
