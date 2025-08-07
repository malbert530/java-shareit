package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Получен HTTP-запрос на получение пользователя по id {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Получен HTTP-запрос на создание пользователя: {}", userDto);
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody UserDto user, @PathVariable Long id) {
        log.info("Получен HTTP-запрос на обновление пользователя c id = {} : {}", id, user);
        return userService.update(user, id);
    }

    @DeleteMapping("/{id}")
    public UserDto delete(@PathVariable Long id) {
        log.info("Получен HTTP-запрос на удаление пользователя с id {}", id);
        return userService.delete(id);
    }
}
