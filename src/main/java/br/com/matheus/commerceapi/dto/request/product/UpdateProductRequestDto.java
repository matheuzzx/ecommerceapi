package br.com.matheus.commerceapi.dto.request.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProductRequestDto(
        @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
        String name,

        @Size(max = 2000, message = "Description must be less than 2000 characters")
        String description,

        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        Long categoryId
) {}
