package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto UserDto);

    UserDto updateUser(UserDto UserDto, long id);

    UserDto getUserById(long id);

    List<UserDto> getAllUsers();

    void deleteUser(long id);
}
