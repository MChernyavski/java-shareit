package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemBookingAndCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {

    private Item item;
    private ItemDto itemDto;
    private ItemBookingAndCommentDto itemBookingAndCommentDto;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "Katya", "katya@user.com");
        item = new Item(1L, "itemNameOne", "itemDescriptionOne", true, user, null);
        itemDto = new ItemDto(1L, "itemNameOne", "itemDescriptionOne", true, null);
        itemBookingAndCommentDto = new ItemBookingAndCommentDto(1L, "itemNameOne",
                "itemDescriptionOne", true, null, null, null);
    }

    @Test
    public void toItemDtoTest() {
        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
    }

    @Test
    public void toItemTest() {
        Item itemNew = ItemMapper.toItem(itemDto, user);

        assertEquals(itemNew.getId(), itemDto.getId());
        assertEquals(itemNew.getName(), itemDto.getName());
        assertEquals(itemNew.getDescription(), itemDto.getDescription());
    }

    @Test
    public void toItemBookingAndCommentTest() {
        ItemBookingAndCommentDto itemBookingAndCommentDtoNew = ItemMapper.toItemBookingAndCommentDto(
                item, null, null, null);

        assertEquals(itemBookingAndCommentDtoNew.getId(), item.getId());
        assertEquals(itemBookingAndCommentDtoNew.getName(), item.getName());
        assertEquals(itemBookingAndCommentDtoNew.getDescription(), item.getDescription());
    }
}
