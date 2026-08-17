package br.com.matheus.commerceapi.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Locale;
import java.util.Objects;

public record Email(String value) {

    public Email {
        Objects.requireNonNull(value, "value must not be null");
        String normalized = value.trim();
        if (!EmailValidator.getInstance().isValid(normalized)) {
            throw new IllegalArgumentException("invalid email format");
        }
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("email must be at most 100 characters");
        }
        value = normalized.toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static Email of(String value) {
        return new Email(value);
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}