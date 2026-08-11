package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.payment.CreatePaymentRequestDto;
import br.com.matheus.commerceapi.dto.response.payment.PaymentResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.PaymentMethod;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private static final Long USER_ID = 1L;
    private static final Long PAYMENT_ID = 1L;
    private static final Long ORDER_ID = 1L;

    private UserDetailsImpl createUserDetails() {
        User user = User.builder().id(USER_ID).build();
        return new UserDetailsImpl(user);
    }

    @Nested
    @DisplayName("Create Payment Tests")
    class CreatePaymentTests {

        @Test
        @DisplayName("Should create payment and return 201")
        void shouldCreatePaymentAndReturnCreated() {
            CreatePaymentRequestDto request = new CreatePaymentRequestDto(ORDER_ID, PaymentMethod.PIX);
            UserDetailsImpl userDetails = createUserDetails();

            when(paymentService.createPayment(USER_ID, request)).thenReturn(null);

            ResponseEntity<PaymentResponseDto> result = paymentController.createPayment(userDetails, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("Should propagate 404 when order not found")
        void shouldPropagateNotFoundWhenOrderNotFound() {
            CreatePaymentRequestDto request = new CreatePaymentRequestDto(ORDER_ID, PaymentMethod.PIX);
            UserDetailsImpl userDetails = createUserDetails();

            when(paymentService.createPayment(USER_ID, request))
                    .thenThrow(new NotFoundException("Order not found with id: " + ORDER_ID));

            assertThatThrownBy(() -> paymentController.createPayment(userDetails, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(ORDER_ID));
        }
    }

    @Nested
    @DisplayName("Simulate Callback Tests")
    class SimulateCallbackTests {

        @Test
        @DisplayName("Should simulate gateway callback and return 200")
        void shouldSimulateCallbackAndReturnOk() {
            UserDetailsImpl userDetails = createUserDetails();
            PaymentResponseDto response = mock(PaymentResponseDto.class);

            when(paymentService.simulateGatewayCallback(PAYMENT_ID, USER_ID)).thenReturn(response);

            ResponseEntity<PaymentResponseDto> result = paymentController.simulateCallback(userDetails, PAYMENT_ID);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(response);
        }

        @Test
        @DisplayName("Should propagate 404 when payment not owned")
        void shouldPropagateNotFoundWhenNotOwned() {
            UserDetailsImpl userDetails = createUserDetails();

            when(paymentService.simulateGatewayCallback(PAYMENT_ID, USER_ID))
                    .thenThrow(new NotFoundException("Payment not found with id: " + PAYMENT_ID));

            assertThatThrownBy(() -> paymentController.simulateCallback(userDetails, PAYMENT_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Payment Tests")
    class GetPaymentTests {

        @Test
        @DisplayName("Should get payment and return 200")
        void shouldGetPaymentAndReturnOk() {
            UserDetailsImpl userDetails = createUserDetails();
            PaymentResponseDto response = mock(PaymentResponseDto.class);

            when(paymentService.getPayment(PAYMENT_ID, USER_ID)).thenReturn(response);

            ResponseEntity<PaymentResponseDto> result = paymentController.getPayment(userDetails, PAYMENT_ID);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(response);
        }

        @Test
        @DisplayName("Should propagate 404 when payment not found")
        void shouldPropagateNotFoundWhenPaymentNotFound() {
            UserDetailsImpl userDetails = createUserDetails();

            when(paymentService.getPayment(PAYMENT_ID, USER_ID))
                    .thenThrow(new NotFoundException("Payment not found with id: " + PAYMENT_ID));

            assertThatThrownBy(() -> paymentController.getPayment(userDetails, PAYMENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(PAYMENT_ID));
        }
    }
}