package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@AllArgsConstructor
public class ItemRequest { //запрос вещи
    private Long id;
    private String description;
    private long requestor;
    private LocalDateTime created;

    public ItemRequest() {

    }
}

/*
id — уникальный идентификатор запроса;
description — текст запроса, содержащий описание требуемой вещи;
requestor — пользователь, создавший запрос;
created — дата и время создания запроса.

 */
