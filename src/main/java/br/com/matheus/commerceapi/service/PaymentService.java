package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.payment.CreatePaymentRequestDto;
import br.com.matheus.commerceapi.dto.request.payment.WebhookPaymentEventDto;
import br.com.matheus.commerceapi.dto.response.payment.PaymentResponseDto;
import br.com.matheus.commerceapi.entity.Order;
import br.com.matheus.commerceapi.entity.Payment;
import br.com.matheus.commerceapi.enums.OrderStatus;
import br.com.matheus.commerceapi.enums.PaymentStatus;
import br.com.matheus.commerceapi.exception.ConflictException;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Value("${payment.checkout-url}")
    private String checkoutUrl;

    @Transactional
    public PaymentResponseDto createPayment(Long customerId, CreatePaymentRequestDto request) {
        Order order = orderService.findOrderByCustomer(request.orderId(), customerId);

        if (order.getStatus() != OrderStatus.CREATED) {
            log.warn("Order {} cannot be paid: status is {}", order.getId(), order.getStatus());
            throw new ConflictException("Order must be CREATED to be paid");
        }

        String transactionId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .order(order)
                .method(request.method())
                .status(PaymentStatus.PENDING)
                .amount(order.getTotal())
                .transactionId(transactionId)
                .checkoutUrl(checkoutUrl + "/" + transactionId)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment {} created for order {} via {}, awaiting gateway webhook",
                savedPayment.getId(), order.getId(), request.method());

        return PaymentResponseDto.fromEntity(savedPayment);
    }

    @Transactional
    public PaymentResponseDto handleWebhookEvent(WebhookPaymentEventDto event) {
        Payment payment = findPaymentByTransactionId(event.transactionId());

        if (event.amount().compareTo(payment.getAmount()) != 0) {
            log.warn("Webhook amount mismatch for transaction {}: expected {}, received {}",
                    event.transactionId(), payment.getAmount(), event.amount());
            throw new InvalidArgumentException("Webhook amount does not match payment amount");
        }

        if (payment.getStatus() == PaymentStatus.PENDING) {
            if (event.isSuccess()) {
                markPaymentSucceeded(payment);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.info("Payment {} failed for order {}", payment.getId(), payment.getOrder().getId());
            }
        }

        return PaymentResponseDto.fromEntity(payment);
    }

    @Transactional
    public PaymentResponseDto simulateGatewayCallback(Long paymentId, Long customerId) {
        Payment payment = findPaymentByCustomer(paymentId, customerId);

        WebhookPaymentEventDto event = new WebhookPaymentEventDto(
                UUID.randomUUID().toString(),
                WebhookPaymentEventDto.EVENT_SUCCEEDED,
                payment.getTransactionId(),
                payment.getAmount()
        );

        return handleWebhookEvent(event);
    }

    public PaymentResponseDto getPayment(Long paymentId, Long customerId) {
        Payment payment = findPaymentByCustomer(paymentId, customerId);

        return PaymentResponseDto.fromEntity(payment);
    }

    private Payment findPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for transaction id: {}", transactionId);
                    return new NotFoundException("Payment not found for transaction id: " + transactionId);
                });
    }

    private Payment findPaymentByCustomer(Long paymentId, Long customerId) {
        return paymentRepository.findByIdAndOrder_CustomerId(paymentId, customerId)
                .orElseThrow(() -> {
                    log.warn("Payment not found or not owned: ID {}, customer {}", paymentId, customerId);
                    return new NotFoundException("Payment not found with id: " + paymentId);
                });
    }

    private void markPaymentSucceeded(Payment payment) {
        Order order = payment.getOrder();
        orderService.markOrderAsPaid(order.getId(), order.getCustomer().getId());

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Payment {} succeeded for order {} via {}", payment.getId(), order.getId(), payment.getMethod());
    }
}