package br.com.matheus.commerceapi.dto.response.order;

import br.com.matheus.commerceapi.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
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
