package ru.practicum.shareit.booking.model;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED,
    UNSUPPORTED_STATUS;

    @Component
    public class StateToStringConverter implements Converter<String, State> {
        @Override
        public State convert(String source) {
            try {
                return State.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException exception) {
                return State.UNSUPPORTED_STATUS;
            }
        }
    }
}
