package br.com.matheus.commerceapi.dto.request.auth;

import br.com.matheus.commerceapi.domain.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(
        @NotNull(message = "Email is required")
        Email email,

        @NotBlank(message = "Password is required")
        String password
) {}