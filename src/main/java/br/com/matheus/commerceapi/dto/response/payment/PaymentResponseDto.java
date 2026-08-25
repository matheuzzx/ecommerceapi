package br.com.matheus.commerceapi.dto.response.payment;

import br.com.matheus.commerceapi.domain.Money;
import br.com.matheus.commerceapi.entity.Payment;
import br.com.matheus.commerceapi.enums.PaymentMethod;
import br.com.matheus.commerceapi.enums.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long id,
        Long orderId,
        PaymentMethod method,
        PaymentStatus status,
        Money amount,
        String transactionId,
        String checkoutUrl,
        LocalDateTime paidAt,
        LocalDateTime refundedAt
) {
    public static PaymentResponseDto fromEntity(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getTransactionId(),
                payment.getCheckoutUrl(),
                payment.getPaidAt(),
                payment.getRefundedAt()
        );
    }
}
