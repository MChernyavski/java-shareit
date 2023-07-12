package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;

import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.user.dto.UserDto;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validateUser(user);
        user = userRepository.save(user);
        log.info("Создан новый пользователь: {}.", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(long id, UserDto userDto) {

        User user = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + id));

        if (userDto.getName() != null && !userDto.getName().isEmpty()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + id));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("ERROR: электронная почта пустая");
            throw new ValidateException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.error("ERROR: в электронном почте нет символа @");
            throw new ValidateException("Электронная почта должна содержать символ @");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new NotFoundException("Пользователь не найден" + user.getName());
        }
    }
}
