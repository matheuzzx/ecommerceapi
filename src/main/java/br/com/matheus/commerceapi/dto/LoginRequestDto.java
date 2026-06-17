package br.com.matheus.commerceapi.dto;

public record LoginRequestDto(
        String email,
        String password
) {}
