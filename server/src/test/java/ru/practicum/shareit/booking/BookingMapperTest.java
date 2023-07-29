package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingMapperTest {

    private Booking booking;
    private BookingRequestDto bookingRequestDto;
    private User user;
    private Item item;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "Katya", "katya@user.com");
        item = new Item(1L, "itemNameOne", "itemDescriptionOne", true, user, null);
        booking = new Booking(1L, LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(5), item, user, Status.APPROVED);
        bookingRequestDto = new BookingRequestDto(1L, LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(5), item.getId());
    }

    @Test
    public void toBookingResponseDtoTest() {
        BookingResponseDto responseDto = BookingMapper.toBookingResponseDto(booking);

        assertEquals(responseDto.getId(), booking.getId());
        assertEquals(responseDto.getStart(), booking.getStart());
        assertEquals(responseDto.getEnd(), booking.getEnd());
        assertEquals(responseDto.getItem(), booking.getItem());
        assertEquals(responseDto.getBooker(), booking.getBooker());
        assertEquals(responseDto.getStatus(), booking.getStatus());
    }

    @Test
    public void toBookingRequestDtoTest() {
        BookingRequestDto requestDto = BookingMapper.bookingRequestDto(booking);

        assertEquals(requestDto.getId(), booking.getId());
        assertEquals(requestDto.getStart(), booking.getStart());
        assertEquals(requestDto.getEnd(), booking.getEnd());
        assertEquals(requestDto.getItemId(), booking.getItem().getId());
    }

    @Test
    public void toBooking() {
        Booking booking1 = BookingMapper.toBooking(bookingRequestDto, item, user);

        assertEquals(booking1.getId(), bookingRequestDto.getId());
        assertEquals(booking1.getStart(), bookingRequestDto.getStart());
        assertEquals(booking1.getEnd(), bookingRequestDto.getEnd());
        assertEquals(booking1.getItem().getId(), bookingRequestDto.getItemId());
    }

    @Test
    public void toBookingItemDto() {
        BookingItemDto bookingItemDto1 = BookingMapper.toBookingItemDto(booking);

        assertEquals(bookingItemDto1.getId(), booking.getId());
        assertEquals(bookingItemDto1.getBookerId(), booking.getBooker().getId());
        assertEquals(bookingItemDto1.getStart(), booking.getStart());
        assertEquals(bookingItemDto1.getEnd(), booking.getEnd());
        assertEquals(bookingItemDto1.getStatus(), booking.getStatus());
    }
}
