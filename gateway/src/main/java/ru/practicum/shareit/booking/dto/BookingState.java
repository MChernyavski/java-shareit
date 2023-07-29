package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingState {
    // Все
    ALL,
    // Текущие
    CURRENT,
    // Будущие
    FUTURE,
    // Завершенные
    PAST,
    // Отклоненные
    REJECTED,
    // Ожидающие подтверждения
    WAITING;
    // Неизвестный статус
   // UNSUPPORTED_STATUS;

    public static Optional<BookingState> from(String stringState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

   /* @Component
    public class StateToStringConverter implements Converter<String, BookingState> {
        @Override
        public BookingState convert(String source) {
            try {
                return BookingState.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException exception) {
                return BookingState.UNSUPPORTED_STATUS;
            }
        }
    }
    */
}