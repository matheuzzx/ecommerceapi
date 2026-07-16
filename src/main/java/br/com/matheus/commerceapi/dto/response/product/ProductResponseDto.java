package br.com.matheus.commerceapi.dto.response.product;

import br.com.matheus.commerceapi.dto.response.category.CategorySummaryDto;
import br.com.matheus.commerceapi.dto.response.store.StoreSummaryDto;
import br.com.matheus.commerceapi.entity.Product;
import java.math.BigDecimal;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Boolean active,
        CategorySummaryDto category,
        StoreSummaryDto store
) {
    public static ProductResponseDto fromEntity(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                CategorySummaryDto.fromEntity(product.getCategory()),
                StoreSummaryDto.fromEntity(product.getStore())
        );
    }
}