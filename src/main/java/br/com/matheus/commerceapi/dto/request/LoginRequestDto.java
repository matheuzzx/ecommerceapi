package br.com.matheus.commerceapi.dto.request;

public record LoginRequestDto(
        String email,
        String password
) {}
