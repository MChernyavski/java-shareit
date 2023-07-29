package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto addRequest(long userId, ItemRequestDto itemRequestDto) { //добавление запроса на вещи
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        itemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestWithItemsDto> getAllRequestsByUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));

        List<ItemRequest> requests =
                itemRequestRepository.findAllByRequestorId(userId, Sort.by("created").descending());

        List<Item> items = itemRepository.findAllByRequestIdIn(requests
                .stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList()));

        return getItemRequests(requests, items);
    }

    @Override
    public List<ItemRequestWithItemsDto> getAllRequests(long userId, int from, int size) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));

        List<ItemRequest> requests =
                itemRequestRepository.findAllByRequestorIdNot(userId,
                        PageRequest.of(from, size, Sort.by("created").descending()));

        List<Item> items = itemRepository.findAllByRequestIdIn(requests
                .stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList()));

        return getItemRequests(requests, items);
    }

    @Override
    public ItemRequestWithItemsDto getRequestById(long userId, long requestId) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Отсутствует пользователь c id " + userId));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Отсутствует запрос c таким id " + requestId));

        List<ItemDto> items = itemRepository.findAllByRequestId(itemRequest.getId())
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestWithListItemsDto(itemRequest, items);
    }

    private List<ItemRequestWithItemsDto> getItemRequests(List<ItemRequest> requests, List<Item> items) {
        List<ItemRequestWithItemsDto> itemRequestWithItemsDto = new ArrayList<>();
        for (ItemRequest itemRequest : requests) {
            List<Item> requestItems = items.stream()
                    .filter(i -> i.getRequest().getId().equals(itemRequest.getId()))
                    .collect(Collectors.toList());
            items.removeAll(requestItems);
            List<ItemDto> requestItemsDto = requestItems.stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
            itemRequestWithItemsDto.add(ItemRequestMapper.toItemRequestWithListItemsDto(itemRequest, requestItemsDto));
        }
        return itemRequestWithItemsDto;
    }

}
