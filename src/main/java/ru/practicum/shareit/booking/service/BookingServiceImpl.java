package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;


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
    public final Sort sort = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public BookingResponseDto addBooking(BookingRequestDto bookingRequestDto, long userId) {
        validateBookingTime(bookingRequestDto);
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

        bookingRequestDto.setStatus(Status.WAITING);
        Booking booking = BookingMapper.toBooking(bookingRequestDto, item, user);
        booking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto approveBooking(long userId, long bookingId, boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id {} не найдено" + bookingId));

        Item item = booking.getItem();

        if (item.getOwner().getId() != userId) {
            throw new NotFoundException("Пользователь c id " + userId +
                    "не владелец запрашиваемой вещи и не может апрувнуть бронирование");
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

    @Override
    public List<BookingResponseDto> getAllBookingByUserId(long userId, State state) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id" + userId));

        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, sort);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, time, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, time, time, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, time, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, sort);
                break;
            default:
                throw new BadRequestStateException(state.name());
        }
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllBookingsByOwner(long ownerId, State state) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id" + ownerId));

        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(ownerId, sort);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, time, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(ownerId, time, time, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, time, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, Status.REJECTED, sort);
                break;
            default:
                throw new BadRequestStateException(state.name());
        }
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    private void validateBookingTime(BookingRequestDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidateException("Поле не может быть пустым");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidateException("Дата и время начала бронирования не может быть в прошлом");
        }
        if (bookingDto.getEnd().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidateException("Дата и время окончания бронирования не может быть в прошлом");
        }
    }
}


