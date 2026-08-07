package br.com.matheus.commerceapi.utils;

import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
public class ValidationUtils {
    public void validateRequiredString(Map<String, String> fields) {
        String emptyField = fields.entrySet().stream()
                .filter(entry -> !StringUtils.hasText(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (emptyField != null) {
            throw new InvalidArgumentException(emptyField + " is required");
        }
    }

    public void validateEmailFormat(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            log.warn("⚠️ Invalid email format: {}", email);
            throw new InvalidArgumentException("Email is not valid");
        }
    }
}
