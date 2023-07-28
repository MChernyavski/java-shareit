package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingItemDtoTest {

    @Autowired
    private JacksonTester<BookingItemDto> json;

    @Test
    void testBookingResponseDto() throws Exception {

        BookingItemDto bookingItemDto = new BookingItemDto(
                1L, 2L,
                LocalDateTime.parse("2023-07-12T15:00:00"),
                LocalDateTime.parse("2023-07-22T15:00:00"),
                Status.APPROVED);

        JsonContent<BookingItemDto> result = json.write(bookingItemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-07-12T15:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-07-22T15:00:00");
        assertThat(result).extractingJsonPathValue("$.status").isEqualTo(Status.APPROVED.toString());
    }
}
