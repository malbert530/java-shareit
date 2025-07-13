package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByAvailableTrueAndNameIgnoreCaseContainingOrDescriptionIgnoreCaseContaining(String text1, String text2);

    List<Item> findByOwner(User user);
}
