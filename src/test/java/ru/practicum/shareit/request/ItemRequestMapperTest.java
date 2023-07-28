package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRequestMapperTest {

    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private ItemRequestWithItemsDto itemRequestWithItemsDto;
    private User user;
    private ItemDto itemDto;
    private ItemDto itemDto2;
    List<ItemDto> itemDtos = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        user = new User(1L, "userOne", "userOne@user.com");
        itemRequest = new ItemRequest(1L, "Sofa", user, LocalDateTime.now());
        itemRequestDto = new ItemRequestDto(1L, "Sofa", LocalDateTime.now());
        itemDto = new ItemDto(1L, "Sofa", "Description", true, 1L);
        itemDto2 = new ItemDto(2L, "Fridge", "New", true, null);
        itemDtos.add(itemDto);
        itemDtos.add(itemDto2);
        itemRequestWithItemsDto = new ItemRequestWithItemsDto(1L, "Sofa", user, LocalDateTime.now(), itemDtos);
    }

    @Test
    public void toItemRequestDto() {
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(itemRequest);

        assertEquals(requestDto.getId(), itemRequest.getId());
        assertEquals(requestDto.getDescription(), itemRequest.getDescription());
    }

    @Test
    public void toItemRequest() {
        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto, user);

        assertEquals(request.getId(), itemRequestDto.getId());
        assertEquals(request.getDescription(), itemRequestDto.getDescription());
    }

    @Test
    public void toItemRequestWithListItemsDtoList() {
        ItemRequestWithItemsDto requestWithItemsDto =
                ItemRequestMapper.toItemRequestWithListItemsDto(itemRequest, itemDtos);

        assertEquals(requestWithItemsDto.getId(), itemRequestWithItemsDto.getId());
        assertEquals(requestWithItemsDto.getItems(), itemRequestWithItemsDto.getItems());
    }
}

