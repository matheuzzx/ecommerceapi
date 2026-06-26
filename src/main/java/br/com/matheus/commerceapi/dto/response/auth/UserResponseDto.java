package br.com.matheus.commerceapi.dto.response.auth;

import br.com.matheus.commerceapi.entity.User;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        String role
) {
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserRole().name()
        );
    }
}
