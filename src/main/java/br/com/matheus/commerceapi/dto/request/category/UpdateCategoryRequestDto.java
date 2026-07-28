package br.com.matheus.commerceapi.dto.request.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateCategoryRequestDto(
        @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
        String displayName,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description
) {}