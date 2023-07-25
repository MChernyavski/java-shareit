package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingAndCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private Booking booking1;
    private ItemRequest itemRequestOne;

    @BeforeEach
    public void setUp() {
        userOneBooker = new User(1L, "userOneBooker", "userOne@user.com");
        userTwoOwner = new User(2L, "userTwoOwner", "userTwo@user.com");
        itemOne = new Item(1L, "itemNameOne", "itemDescriptionOne", true, userTwoOwner, null);
        commentOne = new Comment(1L, "text", itemOne, userOneBooker, LocalDateTime.now());
        booking1 = new Booking(1L, LocalDateTime.of(2023, 8, 16, 22, 00),
                LocalDateTime.of(2023, 9, 16, 22, 00), itemOne, userOneBooker, Status.WAITING);

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
        when(bookingRepository.findByItemId(anyLong(), any())).thenReturn(List.of(booking1));
        ItemBookingAndCommentDto itemBookingAndCommentDto = itemService.getItemById(1L, 2L);

        assertNotNull(itemBookingAndCommentDto);
        assertEquals(itemBookingAndCommentDto.getId(), itemOne.getId());
        assertEquals(itemBookingAndCommentDto.getName(), itemOne.getName());
        assertEquals(itemBookingAndCommentDto.getDescription(), itemOne.getDescription());
        assertEquals(itemBookingAndCommentDto.getComments().get(0).getId(), commentOne.getId());
    }


    @Test
    public void getAllItemsByUserTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userTwoOwner));
        when(itemRepository.findAllByOwnerId(anyLong(), any())).thenReturn(List.of(itemOne));
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of(commentOne));
        when(bookingRepository.findByItemIdIn(anyList(), any())).thenReturn(List.of(booking1));
        List<ItemBookingAndCommentDto> items = itemService.getAllItemsByUser(2L, 0, 5);

        assertNotNull(items);
        assertEquals(items.size(), 1);
        assertEquals(items.get(0).getId(), itemOne.getId());
        assertEquals(items.get(0).getName(), itemOne.getName());
        assertEquals(items.get(0).getDescription(), itemOne.getDescription());
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
    public void addCommentTest() {
        when(userRepository.findById(userOneBooker.getId())).thenReturn(Optional.of(userOneBooker));
        when(itemRepository.findById(itemOne.getId())).thenReturn(Optional.of(itemOne));
        when(bookingRepository.findByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(List.of(booking1));
        when(commentRepository.save(any())).thenReturn(commentOne);
        CommentDto commentDto = itemService.addComment(userOneBooker.getId(), itemOne.getId(), CommentDto.builder().
                text("text").build());

        assertNotNull(commentDto);
        assertEquals(commentOne.getId(), commentDto.getId());
        verify(commentRepository, times(1)).save(any());
    }
}



