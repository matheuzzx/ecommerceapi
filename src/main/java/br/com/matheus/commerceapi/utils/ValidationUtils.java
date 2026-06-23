package br.com.matheus.commerceapi.utils;

import br.com.matheus.commerceapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
public class ValidationUtils {
    public static void validateRequired(Map<String, String> fields) {
        String emptyField = fields.entrySet().stream()
                .filter(entry -> !StringUtils.hasText(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (emptyField != null) {
            throw new BusinessException(emptyField + " is required", HttpStatus.BAD_REQUEST);
        }
    }

    public static  void validateEmailFormat(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            log.warn("⚠️ Invalid email format: {}", email);
            throw new BusinessException("Email is not valid", HttpStatus.BAD_REQUEST);
        }
    }
}
