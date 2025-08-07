package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles(profiles = {"test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestRepositoryTest {
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private User user;
    private User requester;
    private ItemRequest requestToSave;
    private final LocalDateTime created = LocalDateTime.of(2025, 1, 1, 1, 10, 0);

    @BeforeEach
    void setData() {
        String name = "Name";
        String email = "name@mail.com";
        user = new User();
        user.setName(name);
        user.setEmail(email);

        userRepository.save(user);

        requester = new User();
        requester.setName("Tom");
        requester.setEmail("tom@mail.com");

        userRepository.save(requester);

        requestToSave = new ItemRequest();
        requestToSave.setRequester(requester);
        requestToSave.setDescription("Need item");
        requestToSave.setCreated(created);

        requestRepository.save(requestToSave);
    }

    @Test
    void findAllByOrderByCreatedDesc() {
        List<ItemRequest> allRequests = requestRepository.findAllByOrderByCreatedDesc();

        assertNotNull(allRequests);
        assertThat(allRequests.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(requestToSave);
    }

    @Test
    void findByRequesterIdOrderByCreatedDesc() {
        List<ItemRequest> allRequests = requestRepository.findByRequesterIdOrderByCreatedDesc(requester.getId());

        assertNotNull(allRequests);
        assertThat(allRequests.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(requestToSave);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }
}
