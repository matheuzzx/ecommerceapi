package br.com.matheus.commerceapi.dto.response.Category;

import br.com.matheus.commerceapi.entity.Category;

public record CategoryResponseDto(
        Long id,
        String name,
        String displayName,
        String description,
        boolean active
) {

    public static CategoryResponseDto fromEntity(Category category){
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDisplayName(),
                category.getDescription(),
                category.isActive()
        );
    }
}
