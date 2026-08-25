package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.domain.Money;
import br.com.matheus.commerceapi.dto.request.payment.CreatePaymentRequestDto;
import br.com.matheus.commerceapi.dto.request.payment.WebhookPaymentEventDto;
import br.com.matheus.commerceapi.dto.response.payment.PaymentResponseDto;
import br.com.matheus.commerceapi.entity.Order;
import br.com.matheus.commerceapi.entity.Payment;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.OrderStatus;
import br.com.matheus.commerceapi.enums.PaymentMethod;
import br.com.matheus.commerceapi.enums.PaymentStatus;
import br.com.matheus.commerceapi.exception.ConflictException;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final Long PAYMENT_ID = 1L;
    private static final String CHECKOUT_URL = "http://localhost:8080/checkout";

    private Order createOrder() {
        return Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.CREATED)
                .total(Money.of(new BigDecimal("100.00")))
                .customer(User.builder().id(USER_ID).build())
                .build();
    }

    private Payment createPendingPayment(Order order) {
        return Payment.builder()
                .id(PAYMENT_ID)
                .order(order)
                .method(PaymentMethod.PIX)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotal())
                .transactionId("txn-123")
                .checkoutUrl(CHECKOUT_URL + "/txn-123")
                .build();
    }

    @Nested
    @DisplayName("Create Payment Tests")
    class CreatePaymentTests {

        @Test
        @DisplayName("Should create pending payment with checkout url")
        void shouldCreatePendingPayment() {
            ReflectionTestUtils.setField(paymentService, "checkoutUrl", CHECKOUT_URL);
            Order order = createOrder();
            CreatePaymentRequestDto request = new CreatePaymentRequestDto(ORDER_ID, PaymentMethod.PIX);

            when(orderService.findOrderByCustomer(ORDER_ID, USER_ID)).thenReturn(order);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PaymentResponseDto result = paymentService.createPayment(USER_ID, request);

            assertThat(result.orderId()).isEqualTo(ORDER_ID);
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.transactionId()).isNotBlank();
            assertThat(result.checkoutUrl()).startsWith(CHECKOUT_URL);
            assertThat(result.paidAt()).isNull();

            verify(orderService, never()).markOrderAsPaid(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should cancel previous pending payment when creating a new attempt")
        void shouldCancelPreviousPendingPayment() {
            ReflectionTestUtils.setField(paymentService, "checkoutUrl", CHECKOUT_URL);
            Order order = createOrder();
            Payment previousPayment = createPendingPayment(order);
            CreatePaymentRequestDto request = new CreatePaymentRequestDto(ORDER_ID, PaymentMethod.CREDIT_CARD);

            when(orderService.findOrderByCustomer(ORDER_ID, USER_ID)).thenReturn(order);
            when(paymentRepository.findAllByOrderIdAndStatus(ORDER_ID, PaymentStatus.PENDING))
                    .thenReturn(List.of(previousPayment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PaymentResponseDto result = paymentService.createPayment(USER_ID, request);

            assertThat(previousPayment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.method()).isEqualTo(PaymentMethod.CREDIT_CARD);
            verify(paymentRepository).saveAll(List.of(previousPayment));
        }

        @Test
        @DisplayName("Should throw Conflict when order is not CREATED")
        void shouldThrowConflictWhenOrderNotCreated() {
            Order order = createOrder();
            order.setStatus(OrderStatus.PAID);
            CreatePaymentRequestDto request = new CreatePaymentRequestDto(ORDER_ID, PaymentMethod.PIX);

            when(orderService.findOrderByCustomer(ORDER_ID, USER_ID)).thenReturn(order);

            assertThatThrownBy(() -> paymentService.createPayment(USER_ID, request))
                    .isInstanceOf(ConflictException.class);

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should propagate exception when order not found")
        void shouldPropagateExceptionWhenOrderNotFound() {
            CreatePaymentRequestDto request = new CreatePaymentRequestDto(ORDER_ID, PaymentMethod.PIX);

            when(orderService.findOrderByCustomer(ORDER_ID, USER_ID))
                    .thenThrow(new NotFoundException("Order not found with id: " + ORDER_ID));

            assertThatThrownBy(() -> paymentService.createPayment(USER_ID, request))
                    .isInstanceOf(NotFoundException.class);

            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Handle Webhook Event Tests")
    class HandleWebhookEventTests {

        @Test
        @DisplayName("Should mark payment PAID and order paid on succeeded event")
        void shouldMarkPaymentPaidOnSucceededEvent() {
            Order order = createOrder();
            Payment payment = createPendingPayment(order);

            when(paymentRepository.findByTransactionId("txn-123")).thenReturn(Optional.of(payment));
            when(orderService.markOrderAsPaid(ORDER_ID, USER_ID)).thenReturn(order);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            WebhookPaymentEventDto event = new WebhookPaymentEventDto(
                    "evt-1", WebhookPaymentEventDto.EVENT_SUCCEEDED, "txn-123", Money.of(new BigDecimal("100.00")));

            PaymentResponseDto result = paymentService.handleWebhookEvent(event);

            assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
            assertThat(result.paidAt()).isNotNull();
            verify(orderService).markOrderAsPaid(ORDER_ID, USER_ID);
        }

        @Test
        @DisplayName("Should mark payment FAILED on failed event without touching order")
        void shouldMarkPaymentFailedOnFailedEvent() {
            Payment payment = createPendingPayment(createOrder());

            when(paymentRepository.findByTransactionId("txn-123")).thenReturn(Optional.of(payment));

            WebhookPaymentEventDto event = new WebhookPaymentEventDto(
                    "evt-1", WebhookPaymentEventDto.EVENT_FAILED, "txn-123", Money.of(new BigDecimal("100.00")));

            PaymentResponseDto result = paymentService.handleWebhookEvent(event);

            assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
            verify(paymentRepository).save(payment);
            verify(orderService, never()).markOrderAsPaid(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should be idempotent when payment already PAID")
        void shouldBeIdempotentWhenAlreadyPaid() {
            Payment payment = createPendingPayment(createOrder());
            payment.setStatus(PaymentStatus.PAID);

            when(paymentRepository.findByTransactionId("txn-123")).thenReturn(Optional.of(payment));

            WebhookPaymentEventDto event = new WebhookPaymentEventDto(
                    "evt-1", WebhookPaymentEventDto.EVENT_SUCCEEDED, "txn-123", Money.of(new BigDecimal("100.00")));

            PaymentResponseDto result = paymentService.handleWebhookEvent(event);

            assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
            verify(orderService, never()).markOrderAsPaid(anyLong(), anyLong());
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should throw when webhook amount does not match payment")
        void shouldThrowWhenAmountMismatch() {
            Payment payment = createPendingPayment(createOrder());

            when(paymentRepository.findByTransactionId("txn-123")).thenReturn(Optional.of(payment));

            WebhookPaymentEventDto event = new WebhookPaymentEventDto(
                    "evt-1", WebhookPaymentEventDto.EVENT_SUCCEEDED, "txn-123", Money.of(new BigDecimal("99.00")));

            assertThatThrownBy(() -> paymentService.handleWebhookEvent(event))
                    .isInstanceOf(InvalidArgumentException.class);

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should throw NotFound when transaction does not exist")
        void shouldThrowNotFoundWhenTransactionNotFound() {
            when(paymentRepository.findByTransactionId("txn-999")).thenReturn(Optional.empty());

            WebhookPaymentEventDto event = new WebhookPaymentEventDto(
                    "evt-1", WebhookPaymentEventDto.EVENT_SUCCEEDED, "txn-999", Money.of(new BigDecimal("100.00")));

            assertThatThrownBy(() -> paymentService.handleWebhookEvent(event))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Simulate Gateway Callback Tests")
    class SimulateGatewayCallbackTests {

        @Test
        @DisplayName("Should simulate succeeded callback and pay order")
        void shouldSimulateCallbackAndPayOrder() {
            Order order = createOrder();
            Payment payment = createPendingPayment(order);

            when(paymentRepository.findByIdAndOrder_CustomerId(PAYMENT_ID, USER_ID))
                    .thenReturn(Optional.of(payment));
            when(paymentRepository.findByTransactionId("txn-123"))
                    .thenReturn(Optional.of(payment));
            when(orderService.markOrderAsPaid(ORDER_ID, USER_ID)).thenReturn(order);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PaymentResponseDto result = paymentService.simulateGatewayCallback(PAYMENT_ID, USER_ID);

            assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
            verify(orderService).markOrderAsPaid(ORDER_ID, USER_ID);
        }

        @Test
        @DisplayName("Should throw NotFound when payment does not belong to customer")
        void shouldThrowNotFoundWhenNotOwned() {
            when(paymentRepository.findByIdAndOrder_CustomerId(PAYMENT_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.simulateGatewayCallback(PAYMENT_ID, USER_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Payment Tests")
    class GetPaymentTests {

        @Test
        @DisplayName("Should return payment when it belongs to customer")
        void shouldReturnPaymentWhenOwned() {
            when(paymentRepository.findByIdAndOrder_CustomerId(PAYMENT_ID, USER_ID))
                    .thenReturn(Optional.of(createPendingPayment(createOrder())));

            PaymentResponseDto result = paymentService.getPayment(PAYMENT_ID, USER_ID);

            assertThat(result.id()).isEqualTo(PAYMENT_ID);
            assertThat(result.orderId()).isEqualTo(ORDER_ID);
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw NotFound when payment does not belong to customer")
        void shouldThrowNotFoundWhenNotOwned() {
            when(paymentRepository.findByIdAndOrder_CustomerId(PAYMENT_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPayment(PAYMENT_ID, USER_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Refund Payment Tests")
    class RefundPaymentTests {

        @Test
        @DisplayName("Should refund a paid payment")
        void shouldRefundPaidPayment() {
            Payment payment = createPendingPayment(createOrder());
            payment.setStatus(PaymentStatus.PAID);

            when(paymentRepository.findByOrderIdAndStatus(ORDER_ID, PaymentStatus.PAID))
                    .thenReturn(Optional.of(payment));
            when(paymentRepository.save(payment)).thenReturn(payment);

            Payment result = paymentService.refundPaymentForOrder(ORDER_ID);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(result.getRefundedAt()).isNotNull();
            verify(paymentRepository).save(payment);
        }

        @Test
        @DisplayName("Should reject refund when paid payment does not exist")
        void shouldRejectRefundWithoutPaidPayment() {
            when(paymentRepository.findByOrderIdAndStatus(ORDER_ID, PaymentStatus.PAID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.refundPaymentForOrder(ORDER_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Paid payment not found");

            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }
}
