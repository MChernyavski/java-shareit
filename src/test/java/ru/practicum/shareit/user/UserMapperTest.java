package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserMapperTest {

    private User user;
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "Katya", "katya@user.com");
        userDto = new UserDto(1L, "Katya", "katya@user.com");
    }

    @Test
    public void toUserDtoTest() {
        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getEmail(), user.getEmail());
    }

    @Test
    public void toUserTest() {
        User user1 = UserMapper.toUser(userDto);

        assertEquals(user1.getId(), userDto.getId());
        assertEquals(user1.getName(), userDto.getName());
        assertEquals(user1.getEmail(), userDto.getEmail());
    }
}
