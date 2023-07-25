package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User userOwner;
    private User userRequestor;
    private ItemRequest itemRequest;
    private Item item;

    @BeforeEach
    public void setUp() {
        userOwner = userRepository.save(new User(1L, "Masha", "userOne@user.com"));

        userRequestor = userRepository.save(new User(2L, "Oleg", "olegkot@user.com"));

        itemRequest = itemRequestRepository.save(new ItemRequest(1L, "NeedNewSofa", userRequestor, LocalDateTime.now().minusMinutes(20)));

        item = itemRepository.save(new Item(1L, "Sofa", "New", true, userOwner, itemRequest));

    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    public void searchTest() {
        String text = "Sofa";

        TypedQuery<Item> query = em.getEntityManager()
                .createQuery(" SELECT i FROM Item i WHERE (upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
                        "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))) AND i.available = true", Item.class);

        List<Item> items = query.setParameter(1, text).getResultList();
        assertNotNull(items);
        assertEquals(items.size(), 1);

        List<Item> itemsSearchResult = itemRepository.search(text, PageRequest.of(0, 10));
        assertNotNull(itemsSearchResult);
        assertEquals(itemsSearchResult.size(), 1);
    }

    @Test
    public void searchButNotFoundTest() {
        String text = "Fridge";

        TypedQuery<Item> query = em.getEntityManager()
                .createQuery(" SELECT i FROM Item i WHERE (upper(i.name) LIKE upper(concat('%', ?1, '%')) " +
                        "OR upper(i.description) LIKE upper(concat('%', ?1, '%'))) AND i.available = true", Item.class);

        List<Item> items = query.setParameter(1, text).getResultList();
        assertNotNull(items);
        assertEquals(items.size(), 0);


        List<Item> itemsSearchResult = itemRepository.search(text, PageRequest.of(0, 10));
        assertNotNull(itemsSearchResult);
        assertEquals(itemsSearchResult.size(), 0);
    }
}
