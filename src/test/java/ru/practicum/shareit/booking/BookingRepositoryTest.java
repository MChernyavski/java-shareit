package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User userOwner;
    private User userBooker;
    private Item item;
    private Booking booking;

    @BeforeEach
    public void setUp() {
        userOwner = userRepository.save(new User(1L, "Masha", "userOne@user.com"));

        userBooker = userRepository.save(new User(2L, "Oleg", "olegkot@user.com"));

        item = itemRepository.save(new Item(1L, "Sofa", "New", true, userOwner, null));

        booking = bookingRepository.save(new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3),
                item, userBooker, Status.WAITING));
    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    public void findByIdAndOwnerTest() {

        TypedQuery<Booking> query = em.getEntityManager()
                .createQuery("SELECT b FROM Booking b " +
                        "INNER JOIN Item i ON b.item.id = i.id " +
                        "WHERE i.owner.id = :ownerId " +
                        "AND b.id = :bookingId", Booking.class);

        List<Booking> bookingListResult = query.setParameter("ownerId", userOwner.getId())
                .setParameter("bookingId", booking.getId()).getResultList();

        assertNotNull(bookingListResult);
        assertEquals(bookingListResult.size(), 1);

        Booking booking1 = bookingRepository.findByIdAndOwnerId(booking.getId(), userOwner.getId());
        assertNotNull(booking1);
        assertEquals(booking1, booking);
    }
}
