package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.payment.CreatePaymentRequestDto;
import br.com.matheus.commerceapi.dto.response.payment.PaymentResponseDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payments", description = "Payment creation and status for the authenticated CUSTOMER")
public interface PaymentApi {

    @Operation(summary = "Create a payment",
            description = "Creates a PENDING payment for one of the customer's orders and returns a simulated checkout URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment created",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order is not CREATED",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<PaymentResponseDto> createPayment(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @RequestBody @Valid CreatePaymentRequestDto request);

    @Operation(summary = "Simulate gateway callback",
            description = "Simulates the payment gateway confirming a successful payment for one of the customer's payments.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment marked as PAID",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<PaymentResponseDto> simulateCallback(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long paymentId);

    @Operation(summary = "Get a payment", description = "Returns the current status of one of the customer's payments.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment returned",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<PaymentResponseDto> getPayment(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long paymentId);
}
