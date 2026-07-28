package br.com.matheus.commerceapi.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequestDto(
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
        String name,

        @NotBlank(message = "Product description is required")
        @Size(max = 2000, message = "Description must be less than 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Store is required")
        Long storeId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity
) {}
