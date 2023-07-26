package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;
    private User userOneBooker;
    private User userTwoOwner;
    private Item itemOne;
    private Comment commentOne;
    private Comment commentTwo;
    private Booking booking1;
    private Booking booking2;
    private CommentDto commentDto1;
    private ItemDto itemDto;

    @BeforeEach
    public void setUp() {
        userOneBooker = new User(1L, "userOneBooker", "userOne@user.com");
        userTwoOwner = new User(2L, "userTwoOwner", "userTwo@user.com");
        itemOne = new Item(1L, "itemNameOne", "itemDescriptionOne", true, userTwoOwner, null);
        itemDto = ItemMapper.toItemDto(itemOne);
        commentOne = new Comment(1L, "text", itemOne, userOneBooker, LocalDateTime.now());
        commentTwo = new Comment(2L, "textBig", itemOne, userOneBooker, LocalDateTime.now().minusDays(1));
        commentDto1 = CommentMapper.toCommentDto(commentOne);
        booking1 = new Booking(1L, LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(5), itemOne, userOneBooker, Status.APPROVED);
        booking2 = new Booking(3L,
                LocalDateTime.now().plusDays(15),
                LocalDateTime.now().plusDays(20),
                itemOne, userOneBooker, Status.APPROVED);
    }

    @Test
    public void addItemTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userTwoOwner));
        when(itemRepository.save(any())).thenReturn(itemOne);

        ItemDto itemDto = ItemMapper.toItemDto(itemOne);

        ItemDto itemDto1 = itemService.addItem(userTwoOwner.getId(), itemDto);

        assertNotNull(itemDto1);
        assertEquals(itemDto.getId(), itemDto1.getId());
        assertEquals(itemDto.getName(), itemDto1.getName());
        assertEquals(itemDto.getDescription(), itemDto1.getDescription());
        assertEquals(itemDto.getAvailable(), itemDto1.getAvailable());

        verify(itemRepository, times(1)).save(itemOne);
    }

    @Test
    public void addItem_WhenUserNotFound() {
        ItemDto itemDto = ItemMapper.toItemDto(itemOne);

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> itemService.addItem(3L, itemDto));

        assertEquals("Отсутствует пользователь c id " + 3L, e.getMessage());
    }

    @Test
    public void updateItemTest() {
        long userId = userTwoOwner.getId();
        long itemId = itemOne.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userTwoOwner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemOne));

        String newItemName = "nameItemUpdate";
        String newItemDescription = "newItemDescription";
        itemOne.setName(newItemName);
        itemOne.setDescription(newItemDescription);
        when(itemRepository.save(any())).thenReturn(itemOne);

        ItemDto updateItemDto = ItemDto.builder()
                .name(newItemName)
                .description(newItemDescription)
                .build();
        ItemDto itemDto = itemService.updateItem(userId, itemId, updateItemDto);
        assertNotNull(itemDto);
        assertEquals("nameItemUpdate", itemDto.getName());
        assertEquals("newItemDescription", itemDto.getDescription());
    }

    @Test
    public void getItemByIdTest() {

        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemOne));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(List.of(commentOne));
        when(bookingRepository.findByItemId(anyLong(), any())).thenReturn(List.of(booking1, booking2));

        ItemBookingAndCommentDto itemBookingAndCommentDto = itemService.getItemById(1L, 2L);

        assertNotNull(itemBookingAndCommentDto);
        assertEquals(itemBookingAndCommentDto.getId(), itemOne.getId());
        assertEquals(itemBookingAndCommentDto.getName(), itemOne.getName());
        assertEquals(itemBookingAndCommentDto.getDescription(), itemOne.getDescription());
        assertEquals(itemBookingAndCommentDto.getLastBooking().getId(), booking1.getId());
        assertEquals(itemBookingAndCommentDto.getNextBooking().getId(), booking2.getId());
        assertEquals(itemBookingAndCommentDto.getComments().get(0).getId(), commentOne.getId());
    }

    @Test
    public void getItemById_WhenItemNotFoundTest() {
        long itemId = 999L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(itemId, userTwoOwner.getId()));

        assertEquals("Отсутствует вещь c id " + itemId, e.getMessage());
    }

    @Test
    public void getAllItemsByUserTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userTwoOwner));
        when(itemRepository.findAllByOwnerId(anyLong(), any())).thenReturn(List.of(itemOne));
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of(commentOne));
        when(bookingRepository.findByItemIdIn(anyList(), any())).thenReturn(List.of(booking1, booking2));

        List<ItemBookingAndCommentDto> items = itemService.getAllItemsByUser(2L, 0, 5);

        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getId(), itemOne.getId());
        assertEquals(items.get(0).getName(), itemOne.getName());
        assertEquals(items.get(0).getDescription(), itemOne.getDescription());
        assertEquals(items.get(0).getLastBooking().getId(), booking1.getId());
        assertEquals(items.get(0).getNextBooking().getId(), booking2.getId());
        assertEquals(items.get(0).getComments().get(0).getId(), commentOne.getId());
    }

    @Test
    public void getAllItemsByUser_WhenListEmptyTest() {
        long userId = userTwoOwner.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from, size, Sort.by("id").ascending());
        when(userRepository.findById(userId)).thenReturn(Optional.of(userTwoOwner));
        when(itemRepository.findAllByOwnerId(userId, page)).thenReturn(Collections.emptyList());
        List<ItemBookingAndCommentDto> itemDtos = itemService.getAllItemsByUser(userId, from, size);
        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    @Test
    public void searchByTextTest() {
        ItemDto itemDto = ItemMapper.toItemDto(itemOne);
        when(itemRepository.search(anyString(), any())).thenReturn(List.of(itemOne));
        List<ItemDto> itemDtos = itemService.searchItem("itemDescriptionOne", 0, 10);

        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());
        assertEquals(itemDto.getDescription(), itemDtos.get(0).getDescription());
        verify(itemRepository, times(1)).search(anyString(), any());
    }

    @Test
    public void searchByTestButBlankTest() {
        int from = 0;
        int size = 1;

        String text = "";
        List<ItemDto> itemDtos = itemService.searchItem(text, from, size);
        assertNotNull(itemDtos);
        assertEquals(0, itemDtos.size());
    }

    @Test
    public void addCommentTest() {
        long userId = userOneBooker.getId();
        long itemId = itemOne.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userOneBooker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemOne));
        when(bookingRepository.findByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(List.of(booking1));
        when(commentRepository.save(any())).thenReturn(commentOne);
        CommentDto commentDto = itemService.addComment(userId, itemId, CommentDto.builder()
                .text("text").build());

        assertNotNull(commentDto);
        assertEquals(commentOne.getId(), commentDto.getId());
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    public void addCommentWithFailTest() {
        when(itemRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(itemOne));

        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(userOneBooker));

        when(bookingRepository
                .findByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(any(Long.class), any(Long.class),
                        any(Status.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ValidateException result = assertThrows(ValidateException.class,
                () -> itemService.addComment(1L, 1L, commentDto1));

        assertNotNull(result);
    }
}



