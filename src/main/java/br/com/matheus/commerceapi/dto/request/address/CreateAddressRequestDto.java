package br.com.matheus.commerceapi.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequestDto(
        @NotBlank(message = "Street is required")
        String street,

        String number,

        @NotBlank(message = "City is required")
        @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(min = 2, max = 2, message = "State must have 2 characters")
        String state,

        @NotBlank(message = "Zip code is required")
        @Size(max = 10, message = "Zip code must be less than 10 characters")
        String zipCode
) {}