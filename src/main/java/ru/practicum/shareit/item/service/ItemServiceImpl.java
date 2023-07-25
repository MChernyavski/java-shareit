package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;

import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));
        Item item = ItemMapper.toItem(itemDto, user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId()).orElseThrow(() ->
                    new NotFoundException("Отсутствует такой запрос"));
            item.setRequest(itemRequest);
        }
        item = itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));

        Item item = ItemMapper.toItem(itemDto, user);

        Item currentItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Не найден предмет с id " + itemId));

        if (currentItem.getOwner().getId() != userId) {
            throw new NotFoundException("id пользователя не совпадает с id вещи");
        }

        if (item.getName() != null) {
            currentItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            currentItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            currentItem.setAvailable(item.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(currentItem));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemBookingAndCommentDto getItemById(long itemId, long userId) {

        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Отсутствует вещь c id " + itemId));

        LocalDateTime date = LocalDateTime.now();
        BookingItemDto nextBookings = getNextBooking(itemId, userId, date);
        BookingItemDto lastBookings = getLastBooking(itemId, userId, date);
        List<CommentDto> allComments = getComments(itemId);

        return ItemMapper.toItemBookingAndCommentDto(item,
                lastBookings,
                nextBookings,
                allComments == null ? new ArrayList<>() : allComments);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemBookingAndCommentDto> getAllItemsByUser(long userId, int from, int size) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));

        List<Item> items = itemRepository.findAllByOwnerId(userId,
                PageRequest.of(from, size, Sort.by("id").ascending()));

        if (items.isEmpty()) {
            return Collections.emptyList(); // если список пустой, вернули пустой список
        }
        LocalDateTime date = LocalDateTime.now();

        HashMap<Long, BookingItemDto> nextBookingsForItemList = getNextBookingsForItemList(items.stream()
                .map(Item::getId).collect(Collectors.toList()), userId, date);
        HashMap<Long, BookingItemDto> lastBookingsForItemList = getLastBookingsForItemList(items.stream()
                .map(Item::getId).collect(Collectors.toList()), userId, date);
        HashMap<Long, List<CommentDto>> commentsForAllItems = getCommentsForAllItems(items.stream()
                .map(Item::getId).collect(Collectors.toList()));

        List<ItemBookingAndCommentDto> itemBookingAndCommentDtoList = new ArrayList<>();
        for (Item item : items) {
            itemBookingAndCommentDtoList.add(ItemMapper.toItemBookingAndCommentDto(item,
                    lastBookingsForItemList.get(item.getId()),
                    nextBookingsForItemList.get(item.getId()),
                    commentsForAllItems.get(item.getId()) == null ? new ArrayList<>() : commentsForAllItems.get(item.getId())));
        }
        return itemBookingAndCommentDtoList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItem(String text, int from, int size) {
        List<ItemDto> allItemDto = new ArrayList<>();

        if (text.isBlank()) {
            return allItemDto;
        }
        List<Item> items = itemRepository.search(text, PageRequest.of(from, size, Sort.by("id").ascending()));

        for (Item item : items) {
            if (Objects.equals(item.getAvailable(), true)) {
                allItemDto.add(ItemMapper.toItemDto(item));
            }
        }
        return allItemDto;
    }

    @Override
    public CommentDto addComment(long itemId, long userId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Не найден предмет с id " + itemId));

        User author = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));

        if (bookingRepository.findByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(userId,
                        itemId,
                        Status.APPROVED,
                        LocalDateTime.now())
                .isEmpty()) {
            log.warn("Пользователь с id {} не арендовал вещь с id {}", userId, itemId);
            throw new ValidateException("Пользователь, который раньше не арендовывал вещь, не может оставить комментарий");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    private BookingItemDto getLastBooking(Long itemId, Long userId, LocalDateTime date) {
        List<Booking> lastBookings = bookingRepository.findByItemId(itemId, Sort.by("end").descending());
        for (Booking booking : lastBookings) {
            if (booking.getStart().isBefore(date)
                    && booking.getItem().getOwner().getId().equals(userId)
                    && booking.getStatus().equals(Status.APPROVED))
                return BookingMapper.toBookingItemDto(booking);
        }
        return null;
    }


    private BookingItemDto getNextBooking(Long itemId, Long userId, LocalDateTime date) {
        List<Booking> nextBookings = bookingRepository.findByItemId(itemId, Sort.by("start").ascending());
        for (Booking booking : nextBookings) {
            if (booking.getStart().isAfter(date)
                    && booking.getItem().getOwner().getId().equals(userId)
                    && booking.getStatus().equals(Status.APPROVED))
                return BookingMapper.toBookingItemDto(booking);
        }
        return null;
    }


    private List<CommentDto> getComments(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }


    private HashMap<Long, BookingItemDto> getNextBookingsForItemList(List<Long> itemsIds, Long userId, LocalDateTime date) {
        List<Booking> nextBookingsItems = bookingRepository.findByItemIdIn(itemsIds, Sort.by("start").ascending());
        HashMap<Long, BookingItemDto> itemsBookings = new HashMap<>();
        for (Booking booking : nextBookingsItems) {
            if (!itemsBookings.containsKey(booking.getItem().getId())) {
                if (booking.getStart().isAfter(date)
                        && booking.getItem().getOwner().getId().equals(userId)
                        && booking.getStatus().equals(Status.APPROVED)) {
                    itemsBookings.put(booking.getItem().getId(), BookingMapper.toBookingItemDto(booking));
                }
            }
        }
        return itemsBookings;
    }

    private HashMap<Long, BookingItemDto> getLastBookingsForItemList(List<Long> itemsIds, Long userId, LocalDateTime date) {
        List<Booking> lastBookingsItems = bookingRepository.findByItemIdIn(itemsIds, Sort.by("end").descending());
        HashMap<Long, BookingItemDto> itemsBookings = new HashMap<>();
        for (Booking booking : lastBookingsItems) {
            if (!itemsBookings.containsKey(booking.getItem().getId())) {
                if (booking.getStart().isBefore(date)
                        && booking.getItem().getOwner().getId().equals(userId)
                        && booking.getStatus().equals(Status.APPROVED)) {
                    itemsBookings.put(booking.getItem().getId(), BookingMapper.toBookingItemDto(booking));
                }
            }
        }
        return itemsBookings;
    }

    private HashMap<Long, List<CommentDto>> getCommentsForAllItems(List<Long> itemsIds) {
        List<Comment> allComments = commentRepository.findByItemIdIn(itemsIds);
        List<CommentDto> allCommentsDto = allComments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
        HashMap<Long, List<CommentDto>> commentsForAllItems = new HashMap<>();
        for (CommentDto commentDto : allCommentsDto) {
            Long id = commentDto.getItemId();
            if (!commentsForAllItems.containsKey(id)) {
                commentsForAllItems.put(id, new ArrayList<>());
            }
            commentsForAllItems.get(id).add(commentDto);
        }
        return commentsForAllItems;
    }
}
