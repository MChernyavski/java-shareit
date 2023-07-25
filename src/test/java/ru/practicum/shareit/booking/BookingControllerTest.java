package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserDtoTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.model.State.ALL;
import static ru.practicum.shareit.booking.model.State.WAITING;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
private UserDto userDto;
private ItemDto itemDto;
    @BeforeEach
    public void setUp() {
        User user = new User(1L, "Masha", "userOne@user.com");
        User owner = new User(2L, "Kay", "owner@user.com");
        Item item = new Item(1L, "itemName", "itemDescription", true, user,null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(15),
                item, user, Status.WAITING);
        bookingRequestDto = BookingMapper.bookingRequestDto(booking);
        bookingResponseDto = BookingMapper.toBookingResponseDto(booking);

        /*
        bookingRequestDto = new BookingRequestDto(1L, LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10), 1L);
        bookingResponseDto = new BookingResponseDto(1L, bookingRequestDto.getStart(), bookingRequestDto.getEnd(),
                item, user, Status.WAITING);

         */
    }

    @Test
    public void addBookingTest() throws Exception {
        when(bookingService.addBooking(any(), anyLong())).thenReturn(bookingResponseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void updateBookingTest() throws Exception {
        bookingResponseDto.setStatus(Status.APPROVED);

        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(bookingResponseDto);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .content(mapper.writeValueAsString(bookingResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void getBookingByIdTest() throws Exception {
        when(bookingService.getBookingById(1L, 1L)).thenReturn(bookingResponseDto);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(bookingResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void getAllBookingsByUserTest() throws Exception {
        when(bookingService.getAllBookingByUserId(1L, ALL, 0, 1)).thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("State", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(List.of(bookingResponseDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus().toString()), String.class));
    }

    @Test
    public void getAllBookingsByOwnerTest() throws Exception {

        when(bookingService.getAllBookingsByOwner(2L, ALL, 0, 1)).thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "2")
                        .param("State", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(List.of(bookingResponseDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus().toString()), String.class));
    }
}
