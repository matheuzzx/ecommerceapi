package br.com.matheus.commerceapi.dto.response.order;

import br.com.matheus.commerceapi.domain.Money;
import br.com.matheus.commerceapi.entity.OrderItem;

public record OrderItemResponseDto(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        Money unitPrice,
        Money subtotal
) {
    public static OrderItemResponseDto fromEntity(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
