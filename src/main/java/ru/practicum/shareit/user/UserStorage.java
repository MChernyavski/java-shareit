package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {
    User createUser(User user);

    User update(User user);

    List<User> getAllUsers();

    User getUserById(long id);

    void deleteUser(long id);
}
