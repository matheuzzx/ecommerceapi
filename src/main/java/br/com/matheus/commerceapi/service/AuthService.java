package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.auth.LoginRequestDto;
import br.com.matheus.commerceapi.dto.request.auth.RegisterUserRequestDto;
import br.com.matheus.commerceapi.dto.response.auth.TokenResponseDto;
import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.exception.*;
import br.com.matheus.commerceapi.repository.UserRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ValidationUtils validationUtils;

    public UserResponseDto register(RegisterUserRequestDto request) {

        Map<String, String> fields = new HashMap<>();
        fields.put("Name", request.name());
        fields.put("Email", request.email());
        fields.put("Password", request.password());
        fields.put("Role", request.role());

        validationUtils.validateRequiredString(fields);

        String validatedEmail = validateAndTrimEmail(request.email());
        UserRole role = validateAndGetRole(request.role());

        User user = User.builder()
                .name(request.name())
                .email(validatedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .userRole(role)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getUserRole().toString()
        );
    }

    public TokenResponseDto login(LoginRequestDto request) {

        Map<String, String> fields = new HashMap<>();
        fields.put("Email", request.email());
        fields.put("Password", request.password());

        validationUtils.validateRequiredString(fields);

        String trimmedEmail = request.email().trim();

        User user = validateAndGetUser(trimmedEmail);
        validatePassword(request.password(), user.getPasswordHash());

        String token = jwtService.generateToken(trimmedEmail, user.getUserRole().toString());

        return new TokenResponseDto(token);
    }

    private void validatePassword(String password, String userPassword) {
        if (!passwordEncoder.matches(password, userPassword)) {
            log.warn("Invalid password attempt");
            throw new InvalidCredentialsException();
        }
    }

    private User validateAndGetUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new UserNotFoundException();
                });
    }

    private UserRole validateAndGetRole(String roleStr) {
        try {
            UserRole role = UserRole.valueOf(roleStr.toUpperCase());

            if (role != UserRole.CUSTOMER && role != UserRole.STOREOWNER) {
                log.warn("Invalid role attempted: {}", roleStr);
                throw new InvalidRoleException("Invalid role. Allowed: CUSTOMER, STOREOWNER");
            }

            return role;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role format: {}", roleStr);
            throw new InvalidRoleException("Invalid role. Allowed: CUSTOMER, STOREOWNER");
        }
    }

    private String validateAndTrimEmail(String email) {
        String trimmedEmail = email.trim();
        validationUtils.validateEmailFormat(trimmedEmail);
        validateUniqueEmail(trimmedEmail);
        return trimmedEmail;
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            throw new EmailAlreadyExistsException(email);
        }
    }
}