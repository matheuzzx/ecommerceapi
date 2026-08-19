package br.com.matheus.commerceapi.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Protected endpoints reject unauthenticated requests with 401")
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/orders")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/orders/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/categories")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Public catalog does not require authentication")
    void publicCatalogIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/products")).andExpect(status().isOk());
        mockMvc.perform(get("/products/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Role restrictions are enforced across endpoints")
    void roleRestrictionsAreEnforced() throws Exception {
        String storeOwnerToken = registerAndLogin("Sec Owner", "sec.owner@test.com", "STOREOWNER");
        String customerToken = registerAndLogin("Sec Customer", "sec.customer@test.com", "CUSTOMER");

        mockMvc.perform(get("/admin/categories").header("Authorization", bearer(storeOwnerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/stores/my/orders").header("Authorization", bearer(customerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/stores/my/products").header("Authorization", bearer(customerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(storeOwnerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"storeId":1,"addressId":1,"items":[{"productId":1,"quantity":1}]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Store owner cannot access another store's product")
    void storeOwnerCannotAccessAnotherStoreProduct() throws Exception {
        String ownerAToken = registerAndLogin("Owner A", "owner.a@test.com", "STOREOWNER");
        Long storeAId = createStore(ownerAToken, "Store A", "store.a@test.com");

        String adminToken = login("admin@admin.com", "Admin123!");
        Long categoryId = firstCategoryId(adminToken);

        Long productAId = createProduct(ownerAToken, storeAId, categoryId, "Product A", "10.00", 5);

        String ownerBToken = registerAndLogin("Owner B", "owner.b@test.com", "STOREOWNER");

        mockMvc.perform(get("/stores/my/products/{id}/details", productAId)
                        .header("Authorization", bearer(ownerBToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Customer cannot read or cancel another customer's order")
    void customerCannotAccessAnotherCustomerOrder() throws Exception {
        String ownerToken = registerAndLogin("Owner C", "owner.c@test.com", "STOREOWNER");
        Long storeId = createStore(ownerToken, "Store C", "store.c@test.com");

        String adminToken = login("admin@admin.com", "Admin123!");
        Long categoryId = firstCategoryId(adminToken);

        Long productId = createProduct(ownerToken, storeId, categoryId, "Product C", "10.00", 5);

        String customerAToken = registerAndLogin("Customer A", "customer.a@test.com", "CUSTOMER");
        Long addressId = createAddress(customerAToken);
        Long orderId = createOrder(customerAToken, storeId, addressId, productId, 1);

        String customerBToken = registerAndLogin("Customer B", "customer.b@test.com", "CUSTOMER");

        mockMvc.perform(get("/orders/{id}", orderId).header("Authorization", bearer(customerBToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/orders/{id}/cancel", orderId).header("Authorization", bearer(customerBToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Webhook with invalid signature is rejected with 400")
    void webhookWithInvalidSignatureIsRejected() throws Exception {
        mockMvc.perform(post("/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "invalid-signature")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}