package br.com.matheus.commerceapi.dto.response.order;

import br.com.matheus.commerceapi.dto.response.store.StoreSummaryDto;
import br.com.matheus.commerceapi.entity.Order;
import br.com.matheus.commerceapi.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long customerId,
        StoreSummaryDto store,
        OrderStatus status,
        LocalDateTime date,
        BigDecimal total,
        List<OrderItemResponseDto> items
) {
    public static OrderResponseDto fromEntity(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getCustomer().getId(),
                StoreSummaryDto.fromEntity(order.getStore()),
                order.getStatus(),
                order.getDate(),
                order.getTotal(),
                order.getItems().stream().map(OrderItemResponseDto::fromEntity).toList()
        );
    }
}
