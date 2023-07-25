package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User userOne;
    private User userTwoOwner;
    private Item itemOne;
    private ItemRequest itemRequestOne;

    @BeforeEach
    public void setUp() {
        userOne = new User(1L, "userOne", "userOne@user.com");
        userTwoOwner = new User(2L, "userTwoOwner", "userTwo@user.com");
        itemRequestOne = new ItemRequest(1L, "itemRequestDescription", userOne, LocalDateTime.now());
        itemOne = new Item(1L, "itemNameOne", "itemDescriptionOne", true, userTwoOwner, itemRequestOne);
    }

    @Test
    public void addRequestTest() {
        when(itemRequestRepository.save(any())).thenReturn(itemRequestOne);
        when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequestOne);
        ItemRequestDto itemRequestDtoNew = itemRequestService.addRequest(userOne.getId(), itemRequestDto);


        assertNotNull(itemRequestDtoNew);
        assertEquals(itemRequestOne.getId(), itemRequestDtoNew.getId());
        verify(itemRequestRepository, times(1)).save(any());
    }

    @Test
    public void addRequest_WhenUserNotFoundTest() {
        long userNotCorrectId = 120;
        when(userRepository.findById(userNotCorrectId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.addRequest(userNotCorrectId, ItemRequestMapper.toItemRequestDto(itemRequestOne)));
        assertEquals("Отсутствует пользователь c id " + userNotCorrectId, exception.getMessage());
    }


    @Test
    public void getAllRequestByUserTest() {
        long userId = userOne.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userOne));
        when(itemRequestRepository.findAllByRequestorId(anyLong(), any())).thenReturn(List.of(itemRequestOne));
        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(new ArrayList<>(List.of(itemOne)));

        List<ItemRequestWithItemsDto> itemRequestWithItemsDtos = itemRequestService.getAllRequestsByUser(userId);

        assertNotNull(itemRequestWithItemsDtos);
        assertEquals(1, itemRequestWithItemsDtos.size());

    }

    @Test
    public void getAllRequestsTest() {
        long userId = userTwoOwner.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userTwoOwner));
        when(itemRequestRepository.findAllByRequestorIdNot(anyLong(), any())).thenReturn(List.of(itemRequestOne));
        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(new ArrayList<>(List.of(itemOne)));

        List<ItemRequestWithItemsDto> allRequests = itemRequestService.getAllRequests(userId, 0, 10);
        assertNotNull(allRequests);
        assertEquals(1, allRequests.size());
    }

    @Test
    public void getAllRequests_WhenUserNotFoundTest() {
        long userNotCorrectId = 999;
        when(userRepository.findById(userNotCorrectId)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAllRequests(userNotCorrectId, 0, 10));
        assertEquals("Отсутствует пользователь c id " + userNotCorrectId, exception.getMessage());
    }

    @Test
    public void getRequestByIdTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userOne));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.ofNullable(itemRequestOne));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(List.of(itemOne));

        ItemRequestWithItemsDto itemRequestWithItemsDto = itemRequestService.getRequestById(userOne.getId(), itemRequestOne.getId());

        assertNotNull(itemRequestWithItemsDto);
        assertEquals(itemRequestWithItemsDto.getId(), itemRequestOne.getId());
        assertEquals(itemRequestWithItemsDto.getDescription(), itemRequestOne.getDescription());
        assertEquals(itemRequestWithItemsDto.getRequestor(), itemRequestOne.getRequestor());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
    }
}