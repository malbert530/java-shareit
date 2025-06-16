package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id);
        return userMapper.userToDto(user);
    }

    public UserDto create(UserDto userDto) {
        userRepository.checkEmailExist(userDto.getEmail());
        User newUser = userMapper.dtoToUser(userDto);
        User createdUser = userRepository.create(newUser);
        return userMapper.userToDto(createdUser);
    }

    public UserDto update(User user, Long id) {
        userRepository.findById(id);
        User updatedUser = userRepository.update(user, id);
        return userMapper.userToDto(updatedUser);
    }

    public UserDto delete(Long id) {
        User userToDelete = userRepository.findById(id);
        userRepository.delete(id);
        return userMapper.userToDto(userToDelete);
    }

}
