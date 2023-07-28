package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestWithItemsDto> getAllRequestsByUser(long userId);

    List<ItemRequestWithItemsDto> getAllRequests(long userId, int from, int size);

    ItemRequestWithItemsDto getRequestById(long userId, long requestId);
}
