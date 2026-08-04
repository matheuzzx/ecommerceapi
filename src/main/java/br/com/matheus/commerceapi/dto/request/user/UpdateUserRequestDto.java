package br.com.matheus.commerceapi.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(
        @NotBlank(message = "User name is required")
        @Size(min = 2, max = 50, message = "User name must be between 2 and 50 characters")
        String name
) {}
