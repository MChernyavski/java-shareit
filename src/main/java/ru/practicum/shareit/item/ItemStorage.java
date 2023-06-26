package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(Item item, long userId);

    Item updateItem(Item item);

    Item getItemById(long id);

    List<Item> getAllItems();

    List<Item> getAllItemsByUser(long userId);

    List<Item> itemsSearch(String text);
}
