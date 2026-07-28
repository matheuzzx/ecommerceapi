package br.com.matheus.commerceapi.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequestDto(
        @NotBlank(message = "Display name is required")
        @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
        String displayName,

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must be less than 500 characters")
        String description
) {}
