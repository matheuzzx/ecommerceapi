package br.com.matheus.commerceapi.dto.request.product;

import java.math.BigDecimal;

public record CreateProductRequestDto(
        String name,

        String description,

        BigDecimal price,

        boolean active,

        Long categoryId,

        Long storeId,

        Integer quantity
) {}
