package br.com.matheus.commerceapi.dto.response.store;

import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.entity.Store;

import java.time.Instant;

public record StoreSummaryDto(
        Long id,
        String name
) {
    public static StoreSummaryDto fromEntity(Store store) {
        return new StoreSummaryDto(
                store.getId(),
                store.getName()
        );
    }
}
