package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingAndCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemBookingAndCommentDto getItemById(long itemId, long userId);

    List<ItemBookingAndCommentDto> getAllItemsByUser(long userId, int from, int size);

    List<ItemDto> searchItem(String text, int from, int size);

    CommentDto addComment(long userId, long itemId, CommentDto commentDto);
}
