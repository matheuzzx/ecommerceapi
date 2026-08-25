package br.com.matheus.commerceapi.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class E2eFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Full flow: store, product, order, signed webhook payment, ship and deliver")
    void fullCommerceFlow() throws Exception {
        String ownerToken = registerAndLogin("Owner E2E", "owner.e2e@test.com", "STOREOWNER");
        Long storeId = createStore(ownerToken, "E2E Store", "store.e2e@test.com");

        String adminToken = login("admin@admin.com", "Admin123!");
        Long categoryId = firstCategoryId(adminToken);

        Long productId = createProduct(ownerToken, storeId, categoryId, "E2E Product", "100.00", 10);

        String customerToken = registerAndLogin("Customer E2E", "customer.e2e@test.com", "CUSTOMER");
        Long addressId = createAddress(customerToken);
        Long orderId = createOrder(customerToken, storeId, addressId, productId, 1);

        mockMvc.perform(get("/orders/{id}", orderId).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(10));

        JsonNode payment = createPayment(customerToken, orderId, "PIX");
        String transactionId = payment.get("transactionId").asText();
        BigDecimal amount = payment.get("amount").decimalValue();

        mockMvc.perform(get("/payments/{id}", payment.get("id").asLong()).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        sendSignedWebhook(transactionId, "payment.succeeded", amount);

        mockMvc.perform(get("/payments/{id}", payment.get("id").asLong()).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/orders/{id}", orderId).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        sendSignedWebhook(transactionId, "payment.succeeded", amount);

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(9));

        mockMvc.perform(put("/stores/my/orders/{id}/ship", orderId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mockMvc.perform(put("/stores/my/orders/{id}/deliver", orderId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    @DisplayName("Failed payment keeps order CREATED and cancel frees the reservation")
    void failedPaymentThenCancel() throws Exception {
        String ownerToken = registerAndLogin("Owner Fail", "owner.fail@test.com", "STOREOWNER");
        Long storeId = createStore(ownerToken, "Fail Store", "store.fail@test.com");

        String adminToken = login("admin@admin.com", "Admin123!");
        Long categoryId = firstCategoryId(adminToken);

        Long productId = createProduct(ownerToken, storeId, categoryId, "Fail Product", "50.00", 5);

        String customerToken = registerAndLogin("Customer Fail", "customer.fail@test.com", "CUSTOMER");
        Long addressId = createAddress(customerToken);
        Long orderId = createOrder(customerToken, storeId, addressId, productId, 2);

        JsonNode payment = createPayment(customerToken, orderId, "BOLETO");
        String transactionId = payment.get("transactionId").asText();
        BigDecimal amount = payment.get("amount").decimalValue();

        sendSignedWebhook(transactionId, "payment.failed", amount);

        mockMvc.perform(get("/payments/{id}", payment.get("id").asLong()).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));

        mockMvc.perform(get("/orders/{id}", orderId).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));

        mockMvc.perform(put("/orders/{id}/cancel", orderId).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(5));
    }

    @Test
    @DisplayName("Cancel paid order refunds payment and restores physical stock")
    void cancelPaidOrderRefundsPaymentAndRestoresStock() throws Exception {
        String ownerToken = registerAndLogin("Owner Refund", "owner.refund@test.com", "STOREOWNER");
        Long storeId = createStore(ownerToken, "Refund Store", "store.refund@test.com");

        String adminToken = login("admin@admin.com", "Admin123!");
        Long categoryId = firstCategoryId(adminToken);
        Long productId = createProduct(ownerToken, storeId, categoryId, "Refund Product", "75.00", 5);

        String customerToken = registerAndLogin("Customer Refund", "customer.refund@test.com", "CUSTOMER");
        Long addressId = createAddress(customerToken);
        Long orderId = createOrder(customerToken, storeId, addressId, productId, 2);

        JsonNode payment = createPayment(customerToken, orderId, "PIX");
        sendSignedWebhook(payment.get("transactionId").asText(), "payment.succeeded",
                payment.get("amount").decimalValue());

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(3));

        mockMvc.perform(put("/orders/{id}/cancel", orderId)
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/payments/{id}", payment.get("id").asLong())
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.refundedAt").isNotEmpty());

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(5));
    }

    @Test
    @DisplayName("New payment attempt cancels the previous pending payment")
    void newPaymentAttemptCancelsPreviousPendingPayment() throws Exception {
        String ownerToken = registerAndLogin("Owner Retry", "owner.retry@test.com", "STOREOWNER");
        Long storeId = createStore(ownerToken, "Retry Store", "store.retry@test.com");

        String adminToken = login("admin@admin.com", "Admin123!");
        Long categoryId = firstCategoryId(adminToken);
        Long productId = createProduct(ownerToken, storeId, categoryId, "Retry Product", "40.00", 4);

        String customerToken = registerAndLogin("Customer Retry", "customer.retry@test.com", "CUSTOMER");
        Long addressId = createAddress(customerToken);
        Long orderId = createOrder(customerToken, storeId, addressId, productId, 1);

        JsonNode firstPayment = createPayment(customerToken, orderId, "BOLETO");
        JsonNode secondPayment = createPayment(customerToken, orderId, "PIX");

        mockMvc.perform(get("/payments/{id}", firstPayment.get("id").asLong())
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/payments/{id}", secondPayment.get("id").asLong())
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        sendSignedWebhook(firstPayment.get("transactionId").asText(), "payment.succeeded",
                firstPayment.get("amount").decimalValue());

        mockMvc.perform(get("/orders/{id}", orderId)
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));

        sendSignedWebhook(secondPayment.get("transactionId").asText(), "payment.succeeded",
                secondPayment.get("amount").decimalValue());

        mockMvc.perform(get("/orders/{id}", orderId)
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    private void sendSignedWebhook(String transactionId, String eventType, BigDecimal amount) throws Exception {
        String payload = """
                {"eventId":"evt-%s","eventType":"%s","transactionId":"%s","amount":%s}
                """.formatted(UUID.randomUUID(), eventType, transactionId, amount.toPlainString());

        String signature = webhookSignatureUtils.computeSignature(payload, webhookSecret);

        mockMvc.perform(post("/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());
    }
}
