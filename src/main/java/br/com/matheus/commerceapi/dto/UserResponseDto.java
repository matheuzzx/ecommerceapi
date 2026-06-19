package br.com.matheus.commerceapi.dto;

import br.com.matheus.commerceapi.enums.UserRole;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        UserRole role
) {
}
