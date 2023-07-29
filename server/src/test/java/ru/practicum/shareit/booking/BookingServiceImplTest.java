package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestStateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User userOwner;
    private User userBooker;
    private User user3;
    private Item item;
    private Booking booking;
    private Booking booking2;

    private BookingRequestDto bookingRequest;

    @BeforeEach
    public void setUp() {
        userOwner = new User(1L, "userOne", "userOne@user.com");
        userBooker = new User(2L, "userTwoBooker", "userTwo@user.com");
        user3 = new User(3L, "user3", "user3@user.com");
        item = new Item(1L, "itemNameOne", "itemDescriptionOne", true, userOwner, null);
        booking = new Booking(1L,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                item,
                userBooker,
                Status.WAITING);

        booking2 = new Booking(2L,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(10),
                item,
                userBooker,
                Status.WAITING);

        bookingRequest = new BookingRequestDto(1L,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                1L);
    }

    @Test
    public void addBookingTest() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userBooker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(booking);
        BookingResponseDto bookingResponseDto1 = bookingService.addBooking(bookingRequest, userBooker.getId());

        assertNotNull(bookingResponseDto1);
        assertEquals(bookingResponseDto.getId(), bookingResponseDto1.getId());
        assertEquals(bookingResponseDto.getStart(), bookingResponseDto1.getStart());
        assertEquals(bookingResponseDto.getEnd(), bookingResponseDto1.getEnd());

    }

    @Test
    public void addBooking_WhenBookerIsOwner() {
        long ownerId = userOwner.getId();
        long itemId = item.getId();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(userOwner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(bookingRequest, ownerId));

        assertEquals("Владелец вещи не может забронировать свою же вещь", e.getMessage());
    }

    @Test
    public void addBooking_WhenTimeNotCorrectTest() {
        BookingRequestDto bookingWithNotCorrectTime = new BookingRequestDto(3L,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(1),
                1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userBooker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ValidateException e = assertThrows(ValidateException.class,
                () -> bookingService.addBooking(bookingWithNotCorrectTime, userBooker.getId()));
        assertEquals("Время начала бронирования не может быть позже либо равным времени его окончания",
                e.getMessage());
    }

    @Test
    public void addBooking_WhenItemNotAvailableTest() {
        item.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userBooker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        ValidateException e = assertThrows(ValidateException.class,
                () -> bookingService.addBooking(bookingRequest, userBooker.getId()));
        assertEquals("Вещь не доступна для бронирования", e.getMessage());
    }

    @Test
    public void addBooking_WhenUserNotFoundTest() {
        BookingRequestDto newRequest = BookingMapper.bookingRequestDto(booking2);
        long userId = 4L;
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(newRequest, userId));

        assertNotNull(e);
        assertEquals("Не найден пользователь с id " + userId, e.getMessage());
    }

    @Test
    public void approveBookingTest() {
        when(bookingRepository.findByIdAndOwnerId(anyLong(), anyLong())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(booking);
        BookingResponseDto bookingResponseDto1 = bookingService.approveBooking(userBooker.getId(),
                bookingRequest.getId(), true);

        assertNotNull(bookingResponseDto1);
        assertEquals(bookingResponseDto.getId(), bookingResponseDto1.getId());
        assertEquals(bookingResponseDto.getStart(), bookingResponseDto1.getStart());
        assertEquals(bookingResponseDto.getEnd(), bookingResponseDto1.getEnd());
        Assertions.assertEquals(Status.APPROVED, bookingResponseDto1.getStatus());
    }

    @Test
    public void approveButRejectBookingTest() {
        when(bookingRepository.findByIdAndOwnerId(anyLong(), anyLong())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(booking);
        BookingResponseDto bookingResponseDto1 = bookingService.approveBooking(userBooker.getId(),
                bookingRequest.getId(), false);

        assertNotNull(bookingResponseDto1);
        assertEquals(bookingResponseDto.getId(), bookingResponseDto1.getId());
        assertEquals(bookingResponseDto.getStart(), bookingResponseDto1.getStart());
        assertEquals(bookingResponseDto.getEnd(), bookingResponseDto1.getEnd());
        Assertions.assertEquals(Status.REJECTED, bookingResponseDto1.getStatus());
    }

    @Test
    public void approveBooking_WhenBookingApprovedTest() {
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findByIdAndOwnerId(anyLong(), anyLong())).thenReturn(booking);

        ValidateException e = assertThrows(ValidateException.class,
                () -> bookingService.approveBooking(userBooker.getId(), bookingRequest.getId(), true));
        assertEquals("Статус бронирования изменить нельзя", e.getMessage());
    }

    @Test
    public void approveBooking_WhenBookingNotFoundTest() {
        long bookingId = 4L;
        when(bookingRepository.findByIdAndOwnerId(bookingId, userOwner.getId()))
                .thenThrow(new NotFoundException("Не найдено бронирование с id " + bookingId));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(userOwner.getId(), bookingId, true));
        assertEquals("Не найдено бронирование с id " + bookingId, e.getMessage());
    }

    @Test
    public void getBookingByIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userBooker));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));

        BookingResponseDto bookingResponseDto = bookingService.getBookingById(userBooker.getId(), booking.getId());
        assertNotNull(bookingResponseDto);
        assertEquals(bookingResponseDto.getId(), booking.getId());
    }

    @Test
    public void getBookingByIdOwner() {
        long ownerId = userOwner.getId();
        long bookingId = booking.getId();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(userOwner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto bookingResponseDto = bookingService.getBookingById(ownerId, bookingId);

        assertNotNull(bookingResponseDto);
        assertEquals(bookingId, bookingResponseDto.getId());
    }

    @Test
    public void getBookingByIdBooker() {
        long bookerId = userBooker.getId();
        long bookingId = booking.getId();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(userOwner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto bookingResponseDto = bookingService.getBookingById(bookerId, bookingId);

        assertNotNull(bookingResponseDto);
        assertEquals(bookingId, bookingResponseDto.getId());
    }

    @Test
    public void getBookingById_WhenUserNotFoundTest() {
        long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingById(userId, booking.getId()));
        assertEquals("Не найден пользователь с id " + userId, e.getMessage());
    }

    @Test
    public void getBookingById_WhenBookingNotFoundTest() {
        long bookingId = 5L;
        when(userRepository.findById(user3.getId())).thenReturn(Optional.ofNullable(user3));
        when(bookingRepository.findById(bookingId))
                .thenThrow(new NotFoundException("Не найдено бронирование с id " + bookingId));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(user3.getId(), bookingId));
        assertEquals("Не найдено бронирование с id " + bookingId, e.getMessage());
    }

    @Test
    public void getBookingById_WhenUserNotBookerOrOwnerTest() {
        when(userRepository.findById(user3.getId())).thenReturn(Optional.of(user3));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.ofNullable(booking));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(user3.getId(), booking.getId()));

        assertEquals("Пользователь с id " + user3.getId() + " не может посмотреть информацию о бронировании",
                e.getMessage());
    }

    @Test
    public void getBookingsByUserIdTest() {
        long userId = userBooker.getId();
        int from = 0;
        int size = 1;
        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userBooker));

        //Unknown state
        BadRequestStateException e = assertThrows(
                BadRequestStateException.class,
                () -> bookingService.getAllBookingByUserId(userId, State.UNSUPPORTED_STATUS, from, size)
        );
        assertEquals("UNSUPPORTED_STATUS", e.getMessage());

        //State All
        when(bookingRepository.findByBookerId(userId, pageRequest)).thenReturn(List.of(booking));
        List<BookingResponseDto> bookingResponseDtos = bookingService.getAllBookingByUserId(userId,
                State.ALL, from, size);

        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //State Past
        booking.setEnd(LocalDateTime.now().minusDays(7));
        when(bookingRepository.findByBookerIdAndEndIsBefore(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking));
        bookingResponseDtos = bookingService.getAllBookingByUserId(userId, State.PAST, from, size);

        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //State Current
        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), (Pageable) any()))
                .thenReturn(List.of(booking2));

        bookingResponseDtos = bookingService.getAllBookingByUserId(userId, State.CURRENT, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //State Future
        booking2.setStart(LocalDateTime.now().plusMinutes(2));
        when(bookingRepository.findByBookerIdAndStartIsAfter(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking2));

        bookingResponseDtos = bookingService.getAllBookingByUserId(userId, State.FUTURE, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //Status Waiting
        when(bookingRepository.findByBookerIdAndStatus(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking));

        bookingResponseDtos = bookingService.getAllBookingByUserId(userId, State.WAITING, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //Status Rejected
        booking2.setStatus(Status.REJECTED);

        when(bookingRepository.findByBookerIdAndStatus(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking2));

        bookingResponseDtos = bookingService.getAllBookingByUserId(userId, State.REJECTED, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());
    }

    @Test
    public void getBookingsByOwnerTest() {
        long userId = userOwner.getId();
        int from = 0;
        int size = 1;
        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "start"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userOwner));

        //Unknown state
        BadRequestStateException e = assertThrows(
                BadRequestStateException.class,
                () -> bookingService.getAllBookingsByOwner(userId, State.UNSUPPORTED_STATUS, from, size)
        );
        assertEquals("UNSUPPORTED_STATUS", e.getMessage());

        //State All
        when(bookingRepository.findByItemOwnerId(userId, pageRequest)).thenReturn(List.of(booking));
        List<BookingResponseDto> bookingResponseDtos = bookingService.getAllBookingsByOwner(userId,
                State.ALL, from, size);

        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //State Past
        booking.setEnd(LocalDateTime.now().minusDays(7));
        when(bookingRepository.findByItemOwnerIdAndEndIsBefore(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking));
        bookingResponseDtos = bookingService.getAllBookingsByOwner(userId, State.PAST, from, size);

        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //State Current
        when(bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), (Pageable) any()))
                .thenReturn(List.of(booking2));

        bookingResponseDtos = bookingService.getAllBookingsByOwner(userId, State.CURRENT, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //State Future
        booking2.setStart(LocalDateTime.now().plusMinutes(2));
        when(bookingRepository.findByItemOwnerIdAndStartIsAfter(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking2));

        bookingResponseDtos = bookingService.getAllBookingsByOwner(userId, State.FUTURE, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //Status Waiting
        when(bookingRepository.findByItemOwnerIdAndStatus(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking));

        bookingResponseDtos = bookingService.getAllBookingsByOwner(userId, State.WAITING, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());

        //Status Rejected
        booking2.setStatus(Status.REJECTED);
        when(bookingRepository.findByItemOwnerIdAndStatus(anyLong(), any(), (Pageable) any()))
                .thenReturn(List.of(booking2));

        bookingResponseDtos = bookingService.getAllBookingsByOwner(userId, State.REJECTED, from, size);
        assertNotNull(bookingResponseDtos);
        assertEquals(1, bookingResponseDtos.size());
    }
}

