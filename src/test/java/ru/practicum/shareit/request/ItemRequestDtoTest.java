package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDto() throws Exception {
        User user1 = new User(1L, "Masha", "masha@user.com");
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "sofa",
                LocalDateTime.now().minusMinutes(20));

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("sofa");
    }
}
