package br.com.matheus.commerceapi.dto.request.store;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStoreRequestDto(
        @NotBlank(message = "Store name is required")
        @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Store email is required")
        @Email(message = "Store email must be valid")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email
) {}