package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestStateException;
import ru.practicum.shareit.exception.NotFoundException;

import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final Sort sort = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public BookingResponseDto addBooking(BookingRequestDto bookingRequestDto, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id " + userId));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Не найден предмет с id " + bookingRequestDto.getItemId()));

        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Владелец вещи не может забронировать свою же вещь");
        }

        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd())
                || bookingRequestDto.getStart().isEqual(bookingRequestDto.getEnd())) {
            throw new ValidateException("Время начала бронирования не может быть позже либо равным времени его окончания");
        }

        if (!item.getAvailable()) {
            throw new ValidateException("Вещь не доступна для бронирования");
        }

        Booking booking = BookingMapper.toBooking(bookingRequestDto, item, user);
        booking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto approveBooking(long userId, long bookingId, boolean approved) {

        Booking booking = bookingRepository.findByIdAndOwnerId(bookingId, userId);

        if (booking == null) {
            throw new NotFoundException("Не найдено бронирование с id " + bookingId);
        }

        Status status;

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidateException("Статус бронирования изменить нельзя");
        }

        if (approved) {
            if (booking.getStatus().equals(Status.APPROVED)) {
                throw new ValidateException("Бронирование уже потверждено");
            }
            status = Status.APPROVED;
        } else {
            if (booking.getStatus().equals(Status.REJECTED)) {
                throw new ValidateException("Бронирование уже отклонено");
            }
            status = Status.REJECTED;
        }

        booking.setStatus(status);
        booking = bookingRepository.save(booking);


        return BookingMapper.toBookingResponseDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingResponseDto getBookingById(long userId, long bookingId) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Не найден пользователь с id " + userId));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Не найдено бронирование с id " + bookingId));

        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();
        if (bookerId == userId || ownerId == userId) {
            return BookingMapper.toBookingResponseDto(booking);
        }
        throw new NotFoundException("Пользователь с id " + userId + " не может посмотреть информацию о бронировании");
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponseDto> getAllBookingByUserId(long userId, State state, int from, int size) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id" + userId));

        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, PageRequest.of(from / size, size, sort));
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, time,
                        PageRequest.of(from, size, sort));
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, time, time,
                        PageRequest.of(from, size, Sort.by("id").ascending()));
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, time,
                        PageRequest.of(from, size, sort));
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING,
                        PageRequest.of(from, size, sort));
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED,
                        PageRequest.of(from, size, sort));
                break;
            default:
                throw new BadRequestStateException(state.name());
        }
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponseDto> getAllBookingsByOwner(long ownerId, State state, int from, int size) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id" + ownerId));

        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(ownerId, PageRequest.of(from, size, sort));
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, time,
                        PageRequest.of(from, size, sort));
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(ownerId, time, time,
                        PageRequest.of(from, size, Sort.by("id").ascending()));
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, time,
                        PageRequest.of(from, size, sort));
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, Status.WAITING,
                        PageRequest.of(from, size, sort));
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, Status.REJECTED,
                        PageRequest.of(from, size, sort));
                break;
            default:
                throw new BadRequestStateException(state.name());
        }
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }
}


