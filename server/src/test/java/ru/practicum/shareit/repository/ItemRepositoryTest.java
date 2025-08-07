package ru.practicum.shareit.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
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
public class ItemRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private Item itemToSave;
    private User user;
    private ItemRequest request;
    private final String itemName = "Item";

    @BeforeEach
    void setData() {
        String name = "Name";
        String email = "name@mail.com";
        user = new User();
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);

        User requester = new User();
        requester.setName("Tom");
        requester.setEmail("tom@mail.com");
        userRepository.save(requester);

        request = new ItemRequest();
        request.setRequester(requester);
        request.setDescription("Need item");
        request.setCreated(LocalDateTime.now());
        requestRepository.save(request);

        itemToSave = new Item();
        itemToSave.setName(itemName);
        itemToSave.setOwner(user);
        itemToSave.setAvailable(true);
        String description = "Description of Item";
        itemToSave.setDescription(description);
        itemToSave.setRequest(request);
        itemRepository.save(itemToSave);
    }

    @Test
    void findByOwner() {
        List<Item> items = itemRepository.findByOwner(user);
        assertNotNull(items);
        assertThat(items.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(itemToSave);
    }

    @Test
    void findByRequestId() {
        List<Item> items = itemRepository.findByRequestId(request.getId());
        assertNotNull(items);
        assertThat(items.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(itemToSave);
    }

    @Test
    void findByRequestIdNotNull() {
        List<Item> items = itemRepository.findByRequestIdNotNull();
        assertNotNull(items);
        assertThat(items.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(itemToSave);
    }

    @Test
    void findByAvailableTrueAndNameIgnoreCaseContainingOrDescriptionIgnoreCaseContaining() {
        List<Item> items = itemRepository.findByAvailableTrueAndNameIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(itemName, itemName);
        assertNotNull(items);
        assertThat(items.getFirst()).usingRecursiveComparison().ignoringFields("id").isEqualTo(itemToSave);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        requestRepository.deleteAll();
    }
}
