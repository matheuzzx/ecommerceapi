package br.com.matheus.commerceapi.dto.request.product;

import java.math.BigDecimal;

public record UpdateProductRequestDto(
        String name,

        String description,

        BigDecimal price,

        Long categoryId
) {}
