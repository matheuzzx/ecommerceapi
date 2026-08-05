package br.com.matheus.commerceapi.dto.request.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateAddressRequestDto(
        @Size(min = 1, message = "Street must not be empty")
        String street,

        String number,

        @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
        String city,

        @Size(min = 2, max = 2, message = "State must have 2 characters")
        String state,

        @Size(max = 10, message = "Zip code must be less than 10 characters")
        String zipCode
) {}