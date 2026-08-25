package br.com.matheus.commerceapi.controller.admin;

import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderAdminController Tests")
class OrderAdminControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderAdminController orderAdminController;

    private static final Long ORDER_ID = 1L;

    @Test
    @DisplayName("Should confirm order and return 200")
    void shouldConfirmOrder() {
        OrderResponseDto response = mock(OrderResponseDto.class);
        when(orderService.confirmOrder(ORDER_ID)).thenReturn(response);

        ResponseEntity<OrderResponseDto> result = orderAdminController.confirmOrder(ORDER_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(orderService).confirmOrder(ORDER_ID);
    }

    @Test
    @DisplayName("Should ship order and return 200")
    void shouldShipOrder() {
        OrderResponseDto response = mock(OrderResponseDto.class);
        when(orderService.shipOrder(ORDER_ID)).thenReturn(response);

        ResponseEntity<OrderResponseDto> result = orderAdminController.shipOrder(ORDER_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(orderService).shipOrder(ORDER_ID);
    }

    @Test
    @DisplayName("Should deliver order and return 200")
    void shouldDeliverOrder() {
        OrderResponseDto response = mock(OrderResponseDto.class);
        when(orderService.deliverOrder(ORDER_ID)).thenReturn(response);

        ResponseEntity<OrderResponseDto> result = orderAdminController.deliverOrder(ORDER_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(orderService).deliverOrder(ORDER_ID);
    }

    @Test
    @DisplayName("Should propagate exception when order not found")
    void shouldPropagateNotFound() {
        when(orderService.confirmOrder(ORDER_ID)).thenThrow(new RuntimeException());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orderAdminController.confirmOrder(ORDER_ID))
                .isInstanceOf(RuntimeException.class);
    }
}
