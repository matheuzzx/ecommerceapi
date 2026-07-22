package br.com.matheus.commerceapi.dto.response.product;

import br.com.matheus.commerceapi.dto.response.category.CategorySummaryDto;
import br.com.matheus.commerceapi.dto.response.store.StoreSummaryDto;
import br.com.matheus.commerceapi.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductDetailsResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Boolean active,
        CategorySummaryDto category,
        StoreSummaryDto store,
        Integer stockQuantity,
        Boolean inStock,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductDetailsResponseDto fromEntity(Product product) {
        return new ProductDetailsResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                CategorySummaryDto.fromEntity(product.getCategory()),
                StoreSummaryDto.fromEntity(product.getStore()),
                product.getStock() != null ? product.getStock().getQuantity() : 0,
                product.getStock() != null && product.getStock().getQuantity() > 0,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}