package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end,
                                                              Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start, LocalDateTime end,
                                                                 Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, Status status, Sort sort);

    List<Booking> findByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(Long bookerId, Long itemId, Status status, LocalDateTime end);

    List<Booking> findByItemId(Long itemId, Sort sort);

    List<Booking> findByItemIdIn(List<Long> itemsIds, Sort sort);

    @Query("select b from Booking b " +
            "inner join Item i on b.item.id = i.id " +
            "where i.owner.id = :ownerId " +
            "and b.id = :bookingId ")
    Booking findByIdAndOwnerId(Long bookingId, Long ownerId);
}
