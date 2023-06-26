package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemStorageImpl implements ItemStorage {

    private Long itemId = 1L;
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Long>> itemsByUser = new HashMap<>();

    @Override
    public Item addItem(Item item, long userId) {
        validateItem(item);
        item.setId(itemId++);
        items.put(item.getId(), item);
        itemsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(item.getId());
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
        if (!items.containsKey(id)) {
            log.error("ERROR: Не существует вещи с таким id {} ", id);
            throw new ValidateException("Отсутствует вещь c id " + id);
        }
        return items.get(id);
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getAllItemsByUser(long userId) {
        List<Long> itemsIdsByUser = itemsByUser.getOrDefault(userId, new ArrayList<>());
        return itemsIdsByUser.stream().map(items::get).collect(Collectors.toList());
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
