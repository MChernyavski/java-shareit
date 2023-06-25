package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long userId) {
        UserDto userOwner = userService.getUserById(userId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(userMapper.toUser(userOwner));
        itemStorage.addItem(item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Item item = itemMapper.toItem(itemDto); // преобразуем ДТО в Итем
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
        return itemMapper.toItemDto(newItem);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return itemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getAllItemsByUser(long userId) {
        return itemStorage.getAllItems()
                .stream()
                .filter(item -> item.getOwner().getId() == userId)
                .map(itemMapper::toItemDto)
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
            allItemDto.add(itemMapper.toItemDto(item));
        }
        return allItemDto;
    }
}
