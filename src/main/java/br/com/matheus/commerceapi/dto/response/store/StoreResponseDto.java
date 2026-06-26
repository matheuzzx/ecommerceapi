package br.com.matheus.commerceapi.dto.response.store;

import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.entity.Store;

import java.time.Instant;

public record StoreResponseDto(
        Long id,
        String name,
        String slug,
        String email,
        Boolean active,
        UserResponseDto storeOwner,
        Instant createdAt,
        Instant updatedAt
) {
    public static StoreResponseDto fromEntity(Store store) {
        return new StoreResponseDto(
                store.getId(),
                store.getName(),
                store.getSlug(),
                store.getEmail(),
                store.isActive(),
                UserResponseDto.fromEntity(store.getStoreOwner()),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}
