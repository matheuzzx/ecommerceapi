package br.com.matheus.commerceapi.dto.request;

public record RegisterUserRequestDto(
        String name,
        String email,
        String password,
        String role
) {}
