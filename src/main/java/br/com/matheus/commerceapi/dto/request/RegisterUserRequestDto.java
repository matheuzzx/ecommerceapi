package br.com.matheus.commerceapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequestDto(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
                message = "Password must contain at least one uppercase, one lowercase, and one number"
        )
        String password,

        @NotBlank(message = "Role is required")
        @Pattern(
                regexp = "^(CUSTOMER|STOREOWNER)$",
                message = "Role must be CUSTOMER or STOREOWNER"
        )
        String role
) {}