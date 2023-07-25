package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {

    private final UserService service;

    @Test
    void getAllUsers() {
        List<UserDto> userDtoList = List.of(
                makeUserDto("katya", "katya@user.com"),
                makeUserDto("vasya", "vasil@user.ru"),
                makeUserDto("kotik", "kotivgorode@notuser.com")
        );

        userDtoList.forEach((service::createUser));

        List<UserDto> userDtoSaved = service.getAllUsers();

        assertThat(userDtoSaved, hasSize(userDtoList.size()));
        for (UserDto userDto : userDtoList) {
            assertThat(userDtoSaved, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(userDto.getName())),
                    hasProperty("email", equalTo(userDto.getEmail()))
            )));
        }
    }

    private UserDto makeUserDto(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);

        return dto;
    }
}
