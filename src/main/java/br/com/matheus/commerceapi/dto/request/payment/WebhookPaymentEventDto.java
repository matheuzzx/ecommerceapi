package br.com.matheus.commerceapi.dto.request.payment;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WebhookPaymentEventDto(
        @NotNull String eventId,
        @NotNull String eventType,
        @NotNull String transactionId,
        @NotNull BigDecimal amount
) {
    public static final String EVENT_SUCCEEDED = "payment.succeeded";
    public static final String EVENT_FAILED = "payment.failed";

    public boolean isSuccess() {
        return EVENT_SUCCEEDED.equals(eventType);
    }
}