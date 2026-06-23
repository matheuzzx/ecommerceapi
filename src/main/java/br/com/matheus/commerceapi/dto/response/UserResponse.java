package br.com.matheus.commerceapi.dto.response;

public record UserResponse(
        Long id,
        String name,
        String email,
        String role
) {}
