package br.com.matheus.commerceapi.dto.request.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateStockRequestDto(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Integer amount
) {}
