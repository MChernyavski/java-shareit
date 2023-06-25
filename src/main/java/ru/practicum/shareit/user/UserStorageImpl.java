package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.*;

@Slf4j
@Component
public class UserStorageImpl implements UserStorage {

    private Long id = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        validateUser(user);
        isEmailExist(user);
        user.setId(id);
        users.put(user.getId(), user);
        id++;
        log.info("Добавили нового пользователя {}", user.getName());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == 0 && !users.containsKey(user.getId())) {
            log.error("ERROR: Не существует пользователя с таким id {} ", user.getId());
            throw new ValidateException("Отсутствует пользователь c id " + user.getId());
        }
        isEmailExist(user);
        User updatedUser = users.get(user.getId());
        if (user.getName() != null && !user.getName().isEmpty()) {
            updatedUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            updatedUser.setEmail(user.getEmail());
        }
        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновили данные пользователя {}", updatedUser.getName());
        return users.get(updatedUser.getId());
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(long id) {
        if (!users.containsKey(id)) {
            log.error("ERROR: Не существует пользователя с таким id {} ", id);
            throw new NotFoundException("Отсутствует пользователь c id " + id);
        }
        return users.get(id);
    }

    @Override
    public void deleteUser(long id) {
        if (!users.containsKey(id)) {
            log.error("ERROR: Не существует пользователя с таким id {} ", id);
            throw new ValidateException("Отсутствует пользователь c id " + id);
        }
        users.remove(id);
        log.info("Удалили данные пользователя {}", users.get(id));
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

    private void isEmailExist(User user) {
        String email = user.getEmail();
        for (User user1 : getAllUsers()) {
            if (user1.getEmail().equals(email)) {
                if (!Objects.equals(user1.getId(), user.getId())) {
                    throw new ConflictException("Пользователь с такой почтой уже существует");
                }
            }
        }
    }
}