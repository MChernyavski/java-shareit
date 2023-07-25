package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

    private User userOne;
    private UserDto userOneDto;

    @BeforeEach
    public void setUp() {
        userOne = new User(1L, "userOne", "userOne@user.com");
        userOneDto = new UserDto(1L, "userOne", "userOne@user.com");
    }

    @Test
    public void toUserDtoTest() {
        UserDto userDto1 = UserMapper.toUserDto(userOne);
        assertEquals(userDto1, userOneDto);
    }

    @Test
    public void toUserTest() {
        User user1 = UserMapper.toUser(userOneDto);
        assertEquals(user1, userOne);
    }
}

