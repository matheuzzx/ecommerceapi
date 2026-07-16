package br.com.matheus.commerceapi.dto.response.category;

import br.com.matheus.commerceapi.entity.Category;

public record CategorySummaryDto(
        Long id,
        String displayName
) {
    public static CategorySummaryDto fromEntity(Category category) {
        return new CategorySummaryDto(
                category.getId(),
                category.getDisplayName()
        );
    }
}
