package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.payment.WebhookPaymentEventDto;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import br.com.matheus.commerceapi.service.PaymentService;
import br.com.matheus.commerceapi.utils.WebhookSignatureUtils;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final PaymentService paymentService;
    private final WebhookSignatureUtils signatureUtils;
    private final ObjectMapper objectMapper;

    @Value("${payment.webhook-secret}")
    private String webhookSecret;

    public WebhookController(PaymentService paymentService,
                             WebhookSignatureUtils signatureUtils,
                             ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.signatureUtils = signatureUtils;
        this.objectMapper = objectMapper;
    }

    @PostMapping(path = "/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody String rawBody) throws IOException {

        if (!signatureUtils.isValid(rawBody, signature, webhookSecret)) {
            throw new InvalidArgumentException("Invalid webhook signature");
        }

        WebhookPaymentEventDto event = objectMapper.readValue(rawBody, WebhookPaymentEventDto.class);
        paymentService.handleWebhookEvent(event);

        return ResponseEntity.ok().build();
    }
}