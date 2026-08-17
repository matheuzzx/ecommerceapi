package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.payment.WebhookPaymentEventDto;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import br.com.matheus.commerceapi.service.PaymentService;
import br.com.matheus.commerceapi.utils.WebhookSignatureUtils;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookController Tests")
class WebhookControllerTest {

    @Mock
    private PaymentService paymentService;

    private final WebhookSignatureUtils signatureUtils = new WebhookSignatureUtils();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebhookController webhookController;

    private static final String SECRET = "webhook-secret-simulado";
    private static final String PAYLOAD = "{\"eventId\":\"evt_1\",\"eventType\":\"payment.succeeded\",\"transactionId\":\"txn_1\",\"amount\":100.00}";

    @BeforeEach
    void setUp() {
        webhookController = new WebhookController(paymentService, signatureUtils, objectMapper);
        ReflectionTestUtils.setField(webhookController, "webhookSecret", SECRET);
    }

    @Test
    @DisplayName("Should accept webhook with valid signature and return 200")
    void shouldAcceptWebhookWithValidSignature() throws Exception {
        String signature = signatureUtils.computeSignature(PAYLOAD, SECRET);

        ResponseEntity<Void> result = webhookController.handlePaymentWebhook(signature, PAYLOAD);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<WebhookPaymentEventDto> captor = ArgumentCaptor.forClass(WebhookPaymentEventDto.class);
        verify(paymentService).handleWebhookEvent(captor.capture());

        WebhookPaymentEventDto event = captor.getValue();
        assertThat(event.eventId()).isEqualTo("evt_1");
        assertThat(event.isSuccess()).isTrue();
        assertThat(event.transactionId()).isEqualTo("txn_1");
        assertThat(event.amount().amount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should reject webhook with invalid signature")
    void shouldRejectWebhookWithInvalidSignature() {
        assertThatThrownBy(() -> webhookController.handlePaymentWebhook("invalid-signature", PAYLOAD))
                .isInstanceOf(InvalidArgumentException.class);

        verify(paymentService, never()).handleWebhookEvent(any(WebhookPaymentEventDto.class));
    }

    @Test
    @DisplayName("Should reject webhook when signature header missing")
    void shouldRejectWebhookWithMissingSignature() {
        assertThatThrownBy(() -> webhookController.handlePaymentWebhook(null, PAYLOAD))
                .isInstanceOf(InvalidArgumentException.class);

        verify(paymentService, never()).handleWebhookEvent(any(WebhookPaymentEventDto.class));
    }

    @Test
    @DisplayName("Should reject webhook signed with different secret")
    void shouldRejectWebhookSignedWithDifferentSecret() {
        String signature = signatureUtils.computeSignature(PAYLOAD, "outro-secret");

        assertThatThrownBy(() -> webhookController.handlePaymentWebhook(signature, PAYLOAD))
                .isInstanceOf(InvalidArgumentException.class);

        verify(paymentService, never()).handleWebhookEvent(any(WebhookPaymentEventDto.class));
    }
}