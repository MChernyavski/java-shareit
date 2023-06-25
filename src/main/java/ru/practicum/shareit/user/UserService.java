package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, long id);

    UserDto getUserById(long id);

    List<UserDto> getAllUsers();

    void deleteUser(long id);
}
