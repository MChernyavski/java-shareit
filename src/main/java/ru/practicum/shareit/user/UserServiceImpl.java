package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userStorage.createUser(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto, long id) {
        userDto.setId(id);
        User updateUser = userStorage.update(userMapper.toUser(userDto));
        return userMapper.toUserDto(updateUser);
    }

    @Override
    public UserDto getUserById(long id) {
        return userMapper.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userStorage.getAllUsers().stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(long id) {
        userStorage.deleteUser(id);
    }
}
