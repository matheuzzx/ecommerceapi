package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.domain.Money;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.*;
import br.com.matheus.commerceapi.enums.OrderStatus;
import br.com.matheus.commerceapi.exception.ConflictException;
import br.com.matheus.commerceapi.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancellationService Tests")
class OrderCancellationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private StockService stockService;

    @InjectMocks
    private OrderCancellationService cancellationService;

    private static final Long ORDER_ID = 1L;
    private static final Long CUSTOMER_ID = 2L;
    private static final Long PRODUCT_ID = 3L;

    @Test
    @DisplayName("Should cancel CREATED order and release its reservation")
    void shouldCancelCreatedOrderAndReleaseReservation() {
        Order order = createOrder(OrderStatus.CREATED);
        mockOrder(order);

        OrderResponseDto result = cancellationService.cancelOrder(ORDER_ID, CUSTOMER_ID);

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        verify(stockService).cancelReservation(PRODUCT_ID, 2);
        verify(stockService, never()).addStock(anyLong(), anyInt());
        verifyNoInteractions(paymentService);
    }

    @Test
    @DisplayName("Should cancel PAID order, refund payment and restore physical stock")
    void shouldCancelPaidOrderRefundAndRestoreStock() {
        Order order = createOrder(OrderStatus.PAID);
        mockOrder(order);

        OrderResponseDto result = cancellationService.cancelOrder(ORDER_ID, CUSTOMER_ID);

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELED);
        verify(paymentService).refundPaymentForOrder(ORDER_ID);
        verify(stockService).addStock(PRODUCT_ID, 2);
        verify(stockService, never()).cancelReservation(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should reject cancellation after shipment")
    void shouldRejectCancellationAfterShipment() {
        Order order = createOrder(OrderStatus.SHIPPED);
        when(orderRepository.findByCustomerIdAndId(CUSTOMER_ID, ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> cancellationService.cancelOrder(ORDER_ID, CUSTOMER_ID))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(paymentService, stockService);
        verify(orderRepository, never()).save(any(Order.class));
    }

    private void mockOrder(Order order) {
        when(orderRepository.findByCustomerIdAndId(CUSTOMER_ID, ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
    }

    private Order createOrder(OrderStatus status) {
        Product product = Product.builder().id(PRODUCT_ID).build();
        Order order = Order.builder()
                .id(ORDER_ID)
                .customer(User.builder().id(CUSTOMER_ID).build())
                .store(Store.builder().id(4L).build())
                .status(status)
                .total(Money.of(new BigDecimal("20.00")))
                .shippingAddress(ShippingAddress.builder()
                        .street("Street").city("City").state("SP").zipCode("01000-000").build())
                .build();
        OrderItem item = OrderItem.builder()
                .id(5L).order(order).product(product).quantity(2)
                .unitPrice(Money.of(new BigDecimal("10.00"))).build();
        order.setItems(List.of(item));
        return order;
    }
}
