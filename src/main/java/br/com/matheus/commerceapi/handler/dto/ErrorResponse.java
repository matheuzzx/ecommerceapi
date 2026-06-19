package br.com.matheus.commerceapi.handler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
    public ErrorResponse(HttpStatus status, String message, String path) {
        this(status.value(), status.getReasonPhrase(), message, path, LocalDateTime.now());
    }
}
