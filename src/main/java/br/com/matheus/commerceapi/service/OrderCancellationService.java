package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.Order;
import br.com.matheus.commerceapi.enums.OrderStatus;
import br.com.matheus.commerceapi.exception.ConflictException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final StockService stockService;

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findByCustomerIdAndId(customerId, orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (!order.canCancel()) {
            throw new ConflictException("Order cannot be canceled in status: " + order.getStatus());
        }

        if (order.getStatus() == OrderStatus.CREATED) {
            releaseReservations(order);
        } else {
            paymentService.refundPaymentForOrder(order.getId());
            restorePhysicalStock(order);
        }

        order.cancel();
        return OrderResponseDto.fromEntity(orderRepository.save(order));
    }

    private void releaseReservations(Order order) {
        order.getItems().forEach(item -> stockService.cancelReservation(
                item.getProduct().getId(), item.getQuantity()));
    }

    private void restorePhysicalStock(Order order) {
        order.getItems().forEach(item -> stockService.addStock(
                item.getProduct().getId(), item.getQuantity()));
    }
}
