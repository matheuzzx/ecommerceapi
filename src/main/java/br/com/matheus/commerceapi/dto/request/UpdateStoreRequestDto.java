package br.com.matheus.commerceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStoreRequestDto(
        @NotBlank(message = "Store name is required")
        @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
        String name
) {}