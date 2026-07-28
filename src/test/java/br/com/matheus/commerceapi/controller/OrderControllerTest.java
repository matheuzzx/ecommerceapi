package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private static final Long USER_ID = 1L;

    private UserDetailsImpl createUserDetails() {
        User user = User.builder().id(USER_ID).build();
        return new UserDetailsImpl(user);
    }

    // ============================================
    // CREATE ORDER TESTS
    // ============================================

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order and return 201")
        void shouldCreateOrderAndReturnCreated() {
            CreateOrderRequestDto request = new CreateOrderRequestDto(1L, List.of());
            UserDetailsImpl userDetails = createUserDetails();

            when(orderService.createOrder(USER_ID, request)).thenReturn(null);

            ResponseEntity<OrderResponseDto> result = orderController.createOrder(userDetails, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("Should propagate 404 when user not found")
        void shouldPropagateNotFoundWhenUserNotFound() {
            CreateOrderRequestDto request = new CreateOrderRequestDto(1L, List.of());
            UserDetailsImpl userDetails = createUserDetails();

            when(orderService.createOrder(USER_ID, request)).thenThrow(new NotFoundException("User not found with id: " + USER_ID));

            assertThatThrownBy(() -> orderController.createOrder(userDetails, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(USER_ID));
        }

        @Test
        @DisplayName("Should propagate 404 when store not found")
        void shouldPropagateNotFoundWhenStoreNotFound() {
            CreateOrderRequestDto request = new CreateOrderRequestDto(1L, List.of());
            UserDetailsImpl userDetails = createUserDetails();

            when(orderService.createOrder(USER_ID, request)).thenThrow(new NotFoundException("Store not found"));

            assertThatThrownBy(() -> orderController.createOrder(userDetails, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Store not found");
        }
    }
}
