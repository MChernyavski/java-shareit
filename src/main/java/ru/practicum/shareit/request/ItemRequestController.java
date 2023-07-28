package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Добавлен новый запрос на вещи");
        return itemRequestService.addRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getAllByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Список запросов пользователя с id {}", userId);
        return itemRequestService.getAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                                        @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.info("Список всех запросов");
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long requestId) {
        log.info("Информация о запросе с id {}", requestId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}
