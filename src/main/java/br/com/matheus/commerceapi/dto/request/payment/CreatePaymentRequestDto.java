package br.com.matheus.commerceapi.dto.request.payment;

import br.com.matheus.commerceapi.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequestDto(
        @NotNull Long orderId,

        @NotNull PaymentMethod method
) {}