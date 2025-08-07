package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestDtoWithResponses;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto requestToDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setRequesterId(request.getRequester().getId());
        return dto;
    }

    public static ItemRequestDtoWithResponses toDtoWithResponses(ItemRequest request, List<ItemResponseDto> items) {
        ItemRequestDtoWithResponses dto = new ItemRequestDtoWithResponses();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setDescription(request.getDescription());
        dto.setItems(items);
        return dto;
    }

    public static ItemRequest dtoToRequest(ItemRequestDto dto) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setCreated(dto.getCreated());
        return request;
    }
}
