package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Creating new request, id {}", itemRequestDto.getId());
        return itemRequestClient.addRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Getting all requests by user{}", userId);
        return itemRequestClient.getAllRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(name = "from", defaultValue = "0")
                                                 @PositiveOrZero Integer from,
                                                 @RequestParam(name = "size", defaultValue = "10")
                                                 @Positive Integer size) {
        log.info("Getting all requests");
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Getting request {}", requestId);
        return itemRequestClient.getRequestById(userId, requestId);
    }
}
