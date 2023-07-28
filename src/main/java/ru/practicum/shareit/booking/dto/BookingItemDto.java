package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class BookingItemDto {
    private long id;
    private long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private Status status;
}
