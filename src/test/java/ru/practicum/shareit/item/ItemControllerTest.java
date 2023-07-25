package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingAndCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private ItemBookingAndCommentDto itemBookingAndCommentDto;
    private CommentDto commentDto;
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        itemDto = new ItemDto(1L, "itemName", "itemDescription", true, null);
        itemBookingAndCommentDto = new ItemBookingAndCommentDto(2L, "ItemPopular", "Description",
                true, null, null, new ArrayList<>());
        commentDto = new CommentDto(1L, "text", "Masha", LocalDateTime.now(), itemDto.getId());
        userDto = new UserDto(1L, "Masha", "userOne@user.com");
    }

    @Test
    public void addItemTest() throws Exception {
        when(itemService.addItem(anyLong(), any())).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable()), Boolean.class));
    }

    @Test
    public void updateItemTest() throws Exception {
        long userId = 1L;
        ItemDto itemDtoNew = new ItemDto(1L, "updateName", "updateDescription", true, null);

        when(itemService.updateItem(1L, 1L, itemDto)).thenReturn(itemDtoNew);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoNew.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoNew.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDtoNew.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDtoNew.getAvailable()), Boolean.class));
    }

    @Test
    public void getItemByIdTest() throws Exception {
        long userId = 1L;
        when(itemService.getItemById(itemBookingAndCommentDto.getId(), userId))
                .thenReturn(itemBookingAndCommentDto);

        mvc.perform(get("/items/2")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemBookingAndCommentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemBookingAndCommentDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemBookingAndCommentDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemBookingAndCommentDto.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemBookingAndCommentDto.getAvailable()), Boolean.class));
    }

    @Test
    public void getAllItemsTest() throws Exception {

        when(itemService.getAllItemsByUser(1L, 0, 1)).thenReturn(List.of(itemBookingAndCommentDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(List.of(itemBookingAndCommentDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemBookingAndCommentDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemBookingAndCommentDto.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(itemBookingAndCommentDto.getDescription()), String.class))
                .andExpect(jsonPath("$[0].available", is(itemBookingAndCommentDto.getAvailable()), Boolean.class));
    }

    @Test
    public void searchItemTest() throws Exception {

        when(itemService.searchItem("item", 0, 1)).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(List.of(itemDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class));
    }

    @Test
    public void addCommentTest() throws Exception {
        when(itemService.addComment(userDto.getId(), itemDto.getId(), commentDto)).thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText()), String.class))
                .andExpect(jsonPath("$.itemId", is(commentDto.getItemId()), Long.class))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName()), String.class));
    }
}

