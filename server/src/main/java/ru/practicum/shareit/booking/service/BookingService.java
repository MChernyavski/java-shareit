package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {

    BookingResponseDto addBooking(BookingRequestDto bookingRequestDto, long userId);

    BookingResponseDto approveBooking(long userId, long bookingId, boolean approved);

    BookingResponseDto getBookingById(long userId, long bookingId);

    List<BookingResponseDto> getAllBookingByUserId(long userId, State state, int from, int size);

    List<BookingResponseDto> getAllBookingsByOwner(long ownerId, State state, int from, int size);
}
