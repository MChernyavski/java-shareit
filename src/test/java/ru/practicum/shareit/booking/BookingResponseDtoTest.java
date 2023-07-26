package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingResponseDtoTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void testBookingResponseDto() throws Exception {

        BookingResponseDto bookingResponseDto = new BookingResponseDto(
                1L,
                LocalDateTime.parse("2023-07-12T15:00:00"),
                LocalDateTime.parse("2023-07-22T15:00:00"),
                null, null, Status.APPROVED);

        JsonContent<BookingResponseDto> result = json.write(bookingResponseDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-07-12T15:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2023-07-22T15:00:00");
        assertThat(result).extractingJsonPathValue("$.item").isEqualTo(null);
        assertThat(result).extractingJsonPathValue("$.booker").isEqualTo(null);
        assertThat(result).extractingJsonPathValue("$.status").isEqualTo(Status.APPROVED.toString());
    }
}
