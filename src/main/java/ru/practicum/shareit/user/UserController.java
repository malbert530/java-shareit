package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@Validated
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable @Min(1) Long id) {
        log.info("Получен HTTP-запрос на получение пользователя по id {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto create(@RequestBody @Valid UserDto userDto) {
        log.info("Получен HTTP-запрос на создание пользователя: {}", userDto);
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody UserDto user, @PathVariable @Min(1) Long id) {
        log.info("Получен HTTP-запрос на обновление пользователя c id = {} : {}", id, user);
        return userService.update(user, id);
    }

    @DeleteMapping("/{id}")
    public UserDto delete(@PathVariable @Min(1) Long id) {
        log.info("Получен HTTP-запрос на удаление пользователя с id {}", id);
        return userService.delete(id);
    }
}
