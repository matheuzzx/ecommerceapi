package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.payment.CreatePaymentRequestDto;
import br.com.matheus.commerceapi.dto.response.payment.PaymentResponseDto;
import br.com.matheus.commerceapi.docs.controller.PaymentApi;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> createPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreatePaymentRequestDto request) {

        PaymentResponseDto payment = paymentService.createPayment(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PostMapping("/{paymentId}/simulate-callback")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> simulateCallback(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long paymentId) {

        PaymentResponseDto payment = paymentService.simulateGatewayCallback(paymentId, userDetails.getId());

        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> getPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long paymentId) {

        PaymentResponseDto payment = paymentService.getPayment(paymentId, userDetails.getId());

        return ResponseEntity.ok(payment);
    }
}