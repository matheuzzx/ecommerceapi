package br.com.matheus.commerceapi.utils;

import br.com.matheus.commerceapi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Map;

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
}
