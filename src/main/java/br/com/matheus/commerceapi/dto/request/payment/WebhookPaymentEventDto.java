package br.com.matheus.commerceapi.dto.request.payment;

import br.com.matheus.commerceapi.domain.Money;
import jakarta.validation.constraints.NotNull;


public record WebhookPaymentEventDto(
        @NotNull String eventId,
        @NotNull String eventType,
        @NotNull String transactionId,
        @NotNull Money amount
) {
    public static final String EVENT_SUCCEEDED = "payment.succeeded";
    public static final String EVENT_FAILED = "payment.failed";

    public boolean isSuccess() {
        return EVENT_SUCCEEDED.equals(eventType);
    }
}
