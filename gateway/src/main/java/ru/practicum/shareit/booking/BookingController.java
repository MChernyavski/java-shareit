package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody @Valid BookItemRequestDto requestDto) {
        /*  if (!requestDto.getEnd().isAfter(requestDto.getStart())) {
            log.warn("Дата окончания бронирования должна быть после даты начала");
            throw new ValidateException("Дата окончания бронирования должна быть после даты начала");
        }

         */
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.addBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable("bookingId") Long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("Updating booking {}, userId={}, status={}", bookingId, userId, approved);
        return bookingClient.approvedBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getAllBookingsByUserId(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                       Integer from,
                                                       @Positive @RequestParam(name = "size", defaultValue = "10")
                                                       Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getAllBookingsByOwnerId(userId, state, from, size);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<Object> getAllBookingsOfItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                                        @PathVariable(name = "itemId") Long itemId) {
        log.info("Finding bookings od itemId {}, userId={}", itemId, userId);
        return bookingClient.getAllBookingsOfItem(itemId, userId);
    }
}