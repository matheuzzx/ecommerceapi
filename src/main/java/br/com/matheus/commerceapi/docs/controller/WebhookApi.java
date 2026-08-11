package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.payment.WebhookPaymentEventDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;

@Tag(name = "Webhooks", description = "Payment gateway callback endpoint (signed with Stripe-Signature)")
public interface WebhookApi {

    @Operation(summary = "Receive payment webhook",
            description = "Receives a payment event from the gateway. The request is only processed if the "
                    + "Stripe-Signature header is valid for the configured webhook secret.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event processed"),
            @ApiResponse(responseCode = "400", description = "Invalid signature or payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    ResponseEntity<Void> handlePaymentWebhook(
            @Parameter(description = "Gateway signature header (Stripe-Signature format)",
                    schema = @Schema(implementation = String.class))
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody @Parameter(schema = @Schema(implementation = WebhookPaymentEventDto.class))
            String rawBody) throws IOException;
}
