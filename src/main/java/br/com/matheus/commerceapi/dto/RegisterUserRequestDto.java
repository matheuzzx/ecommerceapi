package br.com.matheus.commerceapi.dto;

public record RegisterUserRequestDto(
        String name,
        String email,
        String password,
        String role
) {}
