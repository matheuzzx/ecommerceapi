package br.com.matheus.commerceapi.dto.response.store;

import br.com.matheus.commerceapi.entity.Store;


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
