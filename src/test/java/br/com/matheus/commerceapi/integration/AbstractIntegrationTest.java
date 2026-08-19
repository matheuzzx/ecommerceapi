package br.com.matheus.commerceapi.integration;

import br.com.matheus.commerceapi.utils.WebhookSignatureUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest extends AbstractPostgresTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected WebhookSignatureUtils webhookSignatureUtils;

    @Value("${payment.webhook-secret}")
    protected String webhookSecret;

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String registerAndLogin(String name, String email, String role) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"Password1","role":"%s"}
                                """.formatted(name, email, role)))
                .andExpect(status().isCreated());

        return login(email, "Password1");
    }

    protected String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    protected Long createStore(String token, String name, String email) throws Exception {
        String body = mockMvc.perform(post("/stores")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    protected Long firstCategoryId(String adminToken) throws Exception {
        String body = mockMvc.perform(get("/admin/categories")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("content").get(0).get("id").asLong();
    }

    protected Long createProduct(String token, Long storeId, Long categoryId, String name,
                                 String price, int quantity) throws Exception {
        String body = mockMvc.perform(post("/stores/my/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"Description","price":%s,"categoryId":%d,"storeId":%d,"quantity":%d}
                                """.formatted(name, price, categoryId, storeId, quantity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    protected Long createAddress(String token) throws Exception {
        String body = mockMvc.perform(post("/users/me/addresses")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"street":"Rua das Flores","number":"100","city":"Sao Paulo","state":"SP","zipCode":"01000-000"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    protected Long createOrder(String token, Long storeId, Long addressId, Long productId, int quantity) throws Exception {
        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"storeId":%d,"addressId":%d,"items":[{"productId":%d,"quantity":%d}]}
                                """.formatted(storeId, addressId, productId, quantity)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    protected JsonNode createPayment(String token, Long orderId, String method) throws Exception {
        String body = mockMvc.perform(post("/payments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":%d,\"method\":\"%s\"}".formatted(orderId, method)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body);
    }
}