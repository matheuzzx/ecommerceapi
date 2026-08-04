package br.com.matheus.commerceapi.dto.response.stock;

import br.com.matheus.commerceapi.entity.Stock;

public record StockResponseDto(
        Long productId,
        Integer quantity,
        Integer reserved,
        Integer available
) {
    public static StockResponseDto fromEntity(Stock stock) {
        return new StockResponseDto(
                stock.getProduct().getId(),
                stock.getQuantity(),
                stock.getReserved(),
                stock.getAvailable()
        );
    }
}
