package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private ItemRequestDto itemRequestDto;
    private ItemRequestWithItemsDto itemRequestWithItemsDto;

    @BeforeEach
    public void setUp() {
        User user = new User(1L, "Masha", "userOne@user.com");
        itemRequestDto = new ItemRequestDto(1L, "NeedNewTable", LocalDateTime.now());
        itemRequestWithItemsDto = new ItemRequestWithItemsDto(2L, "FridgeWhite", user, LocalDateTime.now(), new ArrayList<>());
    }

    @Test
    public void addRequestTest() throws Exception {
        when(itemRequestService.addRequest(anyLong(), any())).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription()), String.class));
    }

    @Test
    public void getAllRequestsByUserTest() throws Exception {
        when(itemRequestService.getAllRequestsByUser(anyLong())).thenReturn(List.of(itemRequestWithItemsDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(List.of(itemRequestWithItemsDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestWithItemsDto.getDescription()), String.class));

    }

    @Test
    public void getAllRequestsTest() throws Exception {
        when(itemRequestService.getAllRequests(1L, 0, 1)).thenReturn(List.of(itemRequestWithItemsDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "1")
                        .content(mapper.writeValueAsString(List.of(itemRequestWithItemsDto)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestWithItemsDto.getDescription()), String.class));
    }

    @Test
    public void getRequestByIdTest() throws Exception {
        when(itemRequestService.getRequestById(1L, itemRequestDto.getId())).thenReturn(itemRequestWithItemsDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(itemRequestWithItemsDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestWithItemsDto.getDescription()), String.class));
    }
}

