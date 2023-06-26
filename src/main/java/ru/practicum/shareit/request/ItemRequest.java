package ru.practicum.shareit.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemRequest { //запрос вещи
    private Long id;
    private String description;
    private long requestor;
    private LocalDateTime created;
}

