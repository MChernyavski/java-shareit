package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(long userId, ItemDto itemDto);

    ItemDto getItemById(long itemId);

    List<ItemDto> getAllItemsByUser(long userId);

    List<ItemDto> searchItem(String text);

}
