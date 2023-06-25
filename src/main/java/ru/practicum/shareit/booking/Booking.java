package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
public class Booking {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private long booker;
    private Status status;
}

/*
id — уникальный идентификатор бронирования;
start — дата и время начала бронирования;
end — дата и время конца бронирования;
item — вещь, которую пользователь бронирует;
booker — пользователь, который осуществляет бронирование;
status — статус бронирования. Может принимать одно из следующих
значений: WAITING — новое бронирование, ожидает одобрения, APPROVED —
Дополнительные советы ментора 2
бронирование подтверждено владельцем, REJECTED — бронирование
отклонено владельцем, CANCELED — бронирование отменено создателем.
 */