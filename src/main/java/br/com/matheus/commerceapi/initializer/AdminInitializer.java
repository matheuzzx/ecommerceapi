package br.com.matheus.commerceapi.initializer;

import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@admin.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.name:Administrador}")
    private String adminName;

    @Value("${app.admin.auto-create:true}")
    private boolean autoCreate;

    @Override
    public void run(String... args) {
        if (!autoCreate) {
            log.info("ℹ️ Auto-creation of admin user is disabled.");
            return;
        }

        if (userRepository.existsByUserRole(UserRole.ADMIN)) {
            log.info("✅ Admin user already exists. Email: {}", adminEmail);
            return;
        }

        try {
            User admin = User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .userRole(UserRole.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("✅ Admin user created successfully!");
            log.info("📧 Email: {}", adminEmail);
            log.info("🔑 Password: {}", adminPassword);
            log.info("⚠️ Change this password after first login!");

        } catch (Exception e) {
            log.error("❌ Failed to create admin user: {}", e.getMessage());
        }
    }
}