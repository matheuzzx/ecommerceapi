package br.com.matheus.commerceapi.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractPostgresTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = createContainer();

    private static PostgreSQLContainer<?> createContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("commerceapi_test");
        container.start();
        Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
        return container;
    }
}
