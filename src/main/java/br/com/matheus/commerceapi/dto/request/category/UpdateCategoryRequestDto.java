package br.com.matheus.commerceapi.dto.request.category;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateCategoryRequestDto(
        String displayName,
        String description
) {}