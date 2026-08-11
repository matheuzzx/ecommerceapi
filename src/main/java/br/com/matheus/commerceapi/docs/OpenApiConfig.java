package br.com.matheus.commerceapi.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI commerceApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Commerce API")
                        .description("""
                                E-commerce API with multi-tenant stores, product catalog, stock reservation,
                                order lifecycle and payment integration via signed webhooks.

                                ## Roles
                                - **CUSTOMER** - browses the catalog, creates orders and pays for them.
                                - **STOREOWNER** - manages stores, products, stock and ships/delivers orders.
                                - **ADMIN** - manages categories and drives orders through their lifecycle.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact().name("Matheus Medeiros"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from POST /auth/login")));
    }
}
