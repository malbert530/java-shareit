package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles(profiles = {"test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryTest {
    private final UserRepository userRepository;
    private final String email = "name@mail.com";
    private User userToSave;


    @BeforeEach
    void setData() {
        String name = "Name";
        userToSave = new User();
        userToSave.setName(name);
        userToSave.setEmail(email);
    }

    @Test
    void saveUser() {
        User savedUser = userRepository.save(userToSave);

        assertNotNull(savedUser.getId());
        assertThat(savedUser).usingRecursiveComparison().ignoringFields("id").isEqualTo(userToSave);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveUser_emailAlreadyExist_shouldThrowException() {
        userRepository.save(userToSave);

        User user2 = new User();
        user2.setName("Tom");
        user2.setEmail(email);

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(user2));
    }

    @Test
    void findById() {
        User savedUser = userRepository.save(userToSave);

        User userById = userRepository.findById(savedUser.getId()).get();

        assertThat(savedUser).usingRecursiveComparison().isEqualTo(userById);
    }

    @Test
    void deleteById() {
        User savedUser = userRepository.save(userToSave);

        userRepository.deleteById(savedUser.getId());

        boolean present = userRepository.findById(savedUser.getId()).isPresent();
        assertFalse(present);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }
}
