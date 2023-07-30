package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
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
public class ItemRequestServiceIntegrationTest {

    private final ItemRequestService service;
    private final UserService userService;

    @Test
    void getAllItemRequestsTest() {
        UserDto userDto1 = userService.createUser(UserMapper.toUserDto(makeUser("katya", "katya@user.com")));
        UserDto userDto2 = userService.createUser(UserMapper.toUserDto(makeUser("vasya", "vasil@user.ru")));

        List<ItemRequest> itemRequestList = List.of(
                makeItemRequest("Table", UserMapper.toUser(userDto1)),
                makeItemRequest("Sofa", UserMapper.toUser(userDto1)),
                makeItemRequest("Computer", UserMapper.toUser(userDto1))
        );

        itemRequestList.forEach(itemRequest ->
                service.addRequest(userDto1.getId(), ItemRequestMapper.toItemRequestDto(itemRequest)));

        List<ItemRequestWithItemsDto> itemRequestWithItemsDtos = service.getAllRequests(userDto2.getId(), 0, 10);

        for (ItemRequestWithItemsDto itemRequestWithItemsDto : itemRequestWithItemsDtos) {
            assertThat(itemRequestWithItemsDto.getId(), notNullValue());
            assertThat(itemRequestList, hasItem(hasProperty("description",
                    equalTo(itemRequestWithItemsDto.getDescription()))));
        }
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);

        return user;
    }

    private ItemRequest makeItemRequest(String name, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(name);
        request.setRequestor(requestor);
        return request;
    }
}
