package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Component
public class ItemStorageImpl implements ItemStorage {

    private Long itemId = 1L;
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> itemsByUser = new HashMap<>();

    @Override
    public Item addItem(Item item) {
        validateItem(item);
        item.setId(itemId++);
        items.put(item.getId(), item);
        itemsByUser.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>()).add(item);
        log.info("Добавили новую вещь {}", item.getName());
        return item;
    }


    @Override
    public Item updateItem(Item item) {
        if (items.get(item.getId()) == null) {
            throw new ValidateException("Не существует такой вещи " + item);
        }
        validateItem(item);
        items.put(item.getId(), item);
        log.info("Обновили данные вещи {}", item.getName());
        return item;
    }

    @Override
    public Item getItemById(long id) {
        Item item = items.get(id);
        if (item == null) {
            log.error("ERROR: Не существует вещи с таким id {} ", id);
            throw new ValidateException("Отсутствует вещь c id " + id);
        }
        return item;
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getAllItemsByUser(long userId) {
        return itemsByUser.get(userId);
    }

    @Override
    public List<Item> itemsSearch(String text) {
        List<Item> itemsSearch = new ArrayList<>();
        for (Item item : items.values()) {
            if (Objects.equals(item.getAvailable(), true) &&
                    (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                            item.getDescription().toLowerCase().contains(text.toLowerCase()))) {
                itemsSearch.add(item);
            }
        }
        return itemsSearch;
    }

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isEmpty()) {
            throw new ValidateException("Поле Name не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isEmpty()) {
            throw new ValidateException("Поле Description не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new ValidateException("Полe Available отсутствует");
        }
    }
}
