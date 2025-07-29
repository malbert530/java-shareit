package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithResponses;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userService.getUserIfExistOrElseThrow(userId);
        itemRequestDto.setCreated(LocalDateTime.now());
        ItemRequest request = ItemRequestMapper.dtoToRequest(itemRequestDto);
        request.setRequester(user);
        ItemRequest savedRequest = itemRequestRepository.save(request);
        return ItemRequestMapper.requestToDto(savedRequest);
    }

    public List<ItemRequestDto> getAllRequests() {
        List<ItemRequest> allRequests = itemRequestRepository.findAllByOrderByCreatedDesc();
        return allRequests.stream().map(ItemRequestMapper::requestToDto).toList();
    }

    public List<ItemRequestDtoWithResponses> getAllUserRequests(Long userId) {
        userService.getUserIfExistOrElseThrow(userId);
        List<ItemRequest> allUserRequests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        Map<Long, List<Item>> items = itemRepository.findByRequestIdNotNull().stream()
                .collect(groupingBy(item -> item.getRequest().getId()));

        return allUserRequests.stream()
                .map(request -> {
            List<ItemResponseDto> itemResponses = items.getOrDefault(request.getId(), new ArrayList<>()).stream()
                    .map(ItemMapper::toItemResponseDto)
                    .toList();
            return ItemRequestMapper.toDtoWithResponses(request, itemResponses);
        }).toList();
    }

    public ItemRequestDtoWithResponses getRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("Запроса с id " + requestId + " не существует"));
        List<ItemResponseDto> itemResponses = itemRepository.findByRequestId(requestId).stream()
                .map(ItemMapper::toItemResponseDto).toList();
        return ItemRequestMapper.toDtoWithResponses(itemRequest, itemResponses);
    }
}
