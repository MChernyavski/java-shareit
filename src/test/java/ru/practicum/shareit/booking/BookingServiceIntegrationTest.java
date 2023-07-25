package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.booking.model.State.ALL;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void getAllByOwnerAndState() {
        User userBooker = makeUser("Andrey", "1234@yser.com");
        UserDto userBookerDto = userService.createUser(UserMapper.toUserDto(userBooker));
        User userOwner = makeUser("Vasya", "vasya@user.com");
        UserDto userOwnerDto = userService.createUser(UserMapper.toUserDto(userOwner));

        Item item = makeItem("Frigde", "New", userOwner);
        ItemDto itemDto = itemService.addItem(userOwnerDto.getId(), ItemMapper.toItemDto(item));

        BookingRequestDto bookingRequestDto = makeBookingRequestDto
                (LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(10), itemDto.getId());

        BookingResponseDto bookingResponseDto1 = bookingService.addBooking(bookingRequestDto, userBookerDto.getId());

        List<BookingResponseDto> bookingResponseDtoList = List.of(bookingResponseDto1);

        Long ownerId = userOwnerDto.getId();

        List<BookingResponseDto> getBookingResponseDto =
                bookingService.getAllBookingsByOwner(ownerId, ALL, 0, 10);
        assertThat(getBookingResponseDto, notNullValue());
        assertThat(getBookingResponseDto, hasSize(1));
    }

    private BookingRequestDto makeBookingRequestDto(LocalDateTime start,
                                                    LocalDateTime end,
                                                    long itemId) {

        return BookingRequestDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
    }

    private Item makeItem(String name, String description, User user) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(user);
        return item;
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
