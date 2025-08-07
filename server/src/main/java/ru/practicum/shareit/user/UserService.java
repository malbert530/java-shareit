package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;


    public UserDto getUserById(Long id) {
        User user = getUserIfExistOrElseThrow(id);
        return UserMapper.userToDto(user);
    }

    @Transactional
    public UserDto create(UserDto userDto) {
        User newUser = UserMapper.dtoToUser(userDto);
        User createdUser = saveUser(newUser);
        return UserMapper.userToDto(createdUser);
    }

    @Transactional
    public UserDto update(UserDto user, Long id) {
        User oldUser = getUserIfExistOrElseThrow(id);
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }
        User updatedUser = saveUser(oldUser);
        return UserMapper.userToDto(updatedUser);
    }

    private User saveUser(User newUser) {
        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format("Пользователь с email %s уже существует", newUser.getEmail());
            throw new ValidationException(errorMessage);
        }
    }

    public User getUserIfExistOrElseThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не существует"));
    }

    @Transactional
    public UserDto delete(Long id) {
        User userToDelete = getUserIfExistOrElseThrow(id);
        userRepository.deleteById(id);
        return UserMapper.userToDto(userToDelete);
    }

}
