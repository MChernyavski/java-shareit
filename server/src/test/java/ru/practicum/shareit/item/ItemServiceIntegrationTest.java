package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemBookingAndCommentDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {

    private final ItemService service;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    void getAllItemsByUser() {
        UserDto newUserDto = userService.createUser(UserMapper.toUserDto(makeUser("katya", "katya@user.com")));

        List<Item> itemList = List.of(
                makeItem("table", "new"),
                makeItem("sofa", "red"),
                makeItem("computer", "old")
        );

        itemList.forEach(item -> itemService.addItem(newUserDto.getId(), ItemMapper.toItemDto(item)));

        List<ItemBookingAndCommentDto> itemBookingAndCommentDtoList = itemService.getAllItemsByUser(newUserDto.getId(),
                0, 10);
        for (ItemBookingAndCommentDto items: itemBookingAndCommentDtoList) {
            assertThat(items.getId(), notNullValue());
            assertThat(itemList, hasItem(hasProperty("name", equalTo(items.getName()))));
        }
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);

        return user;
    }

    private Item makeItem(String name, String description) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);

        return item;
    }
}
