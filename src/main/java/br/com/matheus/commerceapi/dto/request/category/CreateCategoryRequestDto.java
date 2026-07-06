package br.com.matheus.commerceapi.dto.request.category;

public record CreateCategoryRequestDto(
        String displayName,
        String description
) {}
