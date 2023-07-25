package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User userOne;
    private User userTwo;

    @BeforeEach
    public void setUp() {
        userOne = new User(1L, "userOne", "userOne@user.com");
        userTwo = new User(2L, "userTwo", "userTwo@user.com");
    }

    @Test
    public void addUserTest() {
        when(userRepository.save(any())).thenReturn(userOne);
        UserDto userDto = UserMapper.toUserDto(userOne);
        UserDto userNew = userServiceImpl.createUser(userDto);

        assertNotNull(userNew);
        assertEquals(userDto.getId(), userNew.getId());
        assertEquals(userDto.getName(), userNew.getName());
        assertEquals(userDto.getEmail(), userNew.getEmail());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void updateUserNameTest() {
        long userId = userOne.getId();
        String updatedName = "nameNew";
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(updatedName);
        updatedUser.setEmail(userOne.getEmail());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userOne));
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserDto updatedUserDto = userServiceImpl.updateUser(userId, UserDto.builder().name(updatedName).build());
        assertNotNull(updatedUserDto);
        assertEquals(userId, updatedUserDto.getId());
        assertEquals(updatedName, updatedUserDto.getName());
    }

    @Test
    public void updateUserEmailTest() {
        long userId = userOne.getId();
        String updatedEmail = "userNew@user.com";
        User updatedUserEmail = new User();
        updatedUserEmail.setId(userId);
        updatedUserEmail.setName(userOne.getName());
        updatedUserEmail.setEmail(updatedEmail);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userOne));
        when(userRepository.save(any())).thenReturn(updatedUserEmail);

        UserDto updatedUserDto = userServiceImpl.updateUser(userId, UserDto.builder().email(updatedEmail).build());
        assertNotNull(updatedUserDto);
        assertEquals(userId, updatedUserDto.getId());
        assertEquals(updatedEmail, updatedUserDto.getEmail());
    }

    @Test
    public void getUserByIdTest() {
        when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
        UserDto userById = userServiceImpl.getUserById(userOne.getId());
        assertNotNull(userById);
        assertEquals(userOne.getId(), userById.getId());
    }

    @Test
    public void getUserByIdNotFoundIdTest() {
        long userNotCorrectId = 25L;
        when(userRepository.findById(userNotCorrectId)).thenReturn(Optional.empty());
        NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> userServiceImpl.getUserById(userNotCorrectId));
        assertEquals("Отсутствует пользователь c id " + userNotCorrectId, e.getMessage());
    }

    @Test
    public void getAllUsersTest() {
        when(userRepository.findAll()).thenReturn(List.of(userOne, userTwo));
        List<UserDto> allUsers = userServiceImpl.getAllUsers();
        assertNotNull(allUsers);
        assertEquals(2, allUsers.size());
    }

    @Test
    public void getAllUsers_WhenListEmptyTest() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> allUsers = userServiceImpl.getAllUsers();
        assertNotNull(allUsers);
        assertEquals(0, allUsers.size());

    }

    @Test
    public void deleteUserTest() {
        userServiceImpl.deleteUser(userOne.getId());
        verify(userRepository, times(1)).deleteById(userOne.getId());
    }
}
