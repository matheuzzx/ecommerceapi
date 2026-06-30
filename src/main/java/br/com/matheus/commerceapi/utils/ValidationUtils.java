package br.com.matheus.commerceapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
public class ValidationUtils {
    public void validateRequired(Map<String, String> fields) {
        String emptyField = fields.entrySet().stream()
                .filter(entry -> !StringUtils.hasText(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (emptyField != null) {
            throw new IllegalArgumentException(emptyField + " is required");
        }
    }

    public void validateEmailFormat(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            log.warn("⚠️ Invalid email format: {}", email);
            throw new IllegalArgumentException("Email is not valid");
        }
    }
}
