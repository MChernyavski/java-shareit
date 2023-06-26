package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userStorage.getUserById(userId));
        itemStorage.addItem(item, userId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto); // преобразуем ДТО в Итем
        Item oldItem = itemStorage.getItemById(item.getId());
        if (oldItem.getOwner().getId() != userId) {
            throw new NotFoundException("id пользователя не совпадает с id вещи");
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        Item newItem = itemStorage.updateItem(oldItem);
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getAllItemsByUser(long userId) {
        return itemStorage.getAllItemsByUser(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        List<ItemDto> allItemDto = new ArrayList<>();
        if (text.isBlank()) {
            return allItemDto;
        }
        List<Item> items = itemStorage.itemsSearch(text);
        for (Item item : items) {
            allItemDto.add(ItemMapper.toItemDto(item));
        }
        return allItemDto;
    }
}
