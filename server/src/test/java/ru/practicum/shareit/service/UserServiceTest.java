package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private UserService service;
    private UserDto userDto;
    private User responseUser;
    private final String name = "Name";
    private final String email = "name@mail.com";
    private final Long id = 22L;

    @BeforeEach
    void setData() {
        service = new UserService(userRepository);

        userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);

        responseUser = new User(id, name, email);
    }

    @Test
    void create() {
        when(userRepository.save(any(User.class))).thenReturn(responseUser);

        UserDto createdUser = service.create(userDto);

        assertEquals(createdUser.getId(), id);
        assertEquals(createdUser.getName(), name);
        assertEquals(createdUser.getEmail(), email);
    }

    @Test
    void create_userExistEmail_shouldThrowException() {
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Error message"));

        assertThrows(ValidationException.class, () -> service.create(userDto));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update() {
        when(userRepository.findById(id)).thenReturn(Optional.of(responseUser));
        when(userRepository.save(any(User.class))).thenReturn(responseUser);

        UserDto updatedUser = service.update(userDto, id);

        assertEquals(updatedUser.getId(), id);
        assertEquals(updatedUser.getName(), name);
        assertEquals(updatedUser.getEmail(), email);
    }

    @Test
    void update_userExistEmail_shouldThrowException() {
        when(userRepository.findById(id)).thenReturn(Optional.of(responseUser));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Error message"));

        assertThrows(ValidationException.class, () -> service.update(userDto, id));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById() {
        when(userRepository.findById(id)).thenReturn(Optional.of(responseUser));

        UserDto userById = service.getUserById(id);

        assertEquals(userById.getId(), id);
        assertEquals(userById.getName(), name);
        assertEquals(userById.getEmail(), email);
    }

    @Test
    void getUserById_shouldThrowExceptionWhenNotFound() {
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getUserById(id));
        verify(userRepository).findById(id);

    }

    @Test
    void delete() {
        when(userRepository.findById(id)).thenReturn(Optional.of(responseUser));

        UserDto deletedUser = service.delete(id);

        assertEquals(deletedUser.getId(), id);
    }
}
