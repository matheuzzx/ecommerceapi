package br.com.matheus.commerceapi.dto.request.store;

import br.com.matheus.commerceapi.domain.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStoreRequestDto(
        @NotBlank(message = "Store name is required")
        @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Store email is required")
        Email email
) {}