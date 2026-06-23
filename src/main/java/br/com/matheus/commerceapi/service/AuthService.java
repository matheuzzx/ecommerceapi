package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.LoginRequestDto;
import br.com.matheus.commerceapi.dto.request.RegisterUserRequestDto;
import br.com.matheus.commerceapi.dto.response.TokenResponse;
import br.com.matheus.commerceapi.dto.response.UserResponse;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.exception.BusinessException;
import br.com.matheus.commerceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static br.com.matheus.commerceapi.utils.ValidationUtils.validateRequired;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse register(RegisterUserRequestDto request){

        log.info("🚀 Starting user registration for email: {}", request.email());

        validateRequired(Map.of(
                "Name", request.name(),
                "Email", request.email(),
                "Password", request.password(),
                "Role", request.role()
        ));

        String validatedEmail = validateAndTrimEmail(request.email());
        UserRole role = validateAndGetRole(request.role());

        log.debug("Email validated: {}, Role: {}", validatedEmail, role);

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .name(request.name())
                .email(validatedEmail)
                .passwordHash(hashedPassword)
                .userRole(role)
                .build();

        User savedUser = userRepository.save(user);

        UserResponse userResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getUserRole().toString()
        );

        log.info("✅ User registered successfully: {} (ID: {})", validatedEmail, savedUser.getId());

        return userResponse;
    }

    public TokenResponse login(LoginRequestDto request){

        log.info("🔐 Login attempt for email: {}", request.email());

        validateRequired(Map.of(
                "Email", request.email(),
                "Password", request.password()
        ));

        String trimmedEmail = request.email().trim();

        log.debug("Searching user by email: {}", trimmedEmail);

        var user = validateAndGetUser(trimmedEmail);

        log.debug("Validating password for user: {}", trimmedEmail);

        validatePassword(request.password(), user.getPasswordHash());

        String token = jwtService.generateToken(trimmedEmail, user.getUserRole().toString());

        log.debug("Validating password for user: {}", trimmedEmail);

        return new TokenResponse(token);
    }

    private void validatePassword(String password, String userPassword){
        if(!passwordEncoder.matches(password, userPassword)) {
            log.warn("❌ Invalid password attempt");
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    private User validateAndGetUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ User not found: {}", email);
                    return new BusinessException("User not registered", HttpStatus.NOT_FOUND);
                });
    }


    private UserRole validateAndGetRole(String roleStr) {

            UserRole role = UserRole.valueOf(roleStr.toUpperCase());

            if (role != UserRole.CUSTOMER && role != UserRole.STOREOWNER) {
                log.warn("⚠️ Invalid role attempted: {}", roleStr);
                throw new BusinessException("Invalid role. Allowed: CUSTOMER, STOREOWNER", HttpStatus.BAD_REQUEST);
            }

            return role;
    }

    private String validateAndTrimEmail(String email){
        String trimmedEmail = returnTrimmedEmail(email);
        validateEmailFormat(trimmedEmail);
        validateUniqueEmail(trimmedEmail);
        return trimmedEmail;
    }

    private void validateEmailFormat(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            log.warn("⚠️ Invalid email format: {}", email);
            throw new BusinessException("Email is not valid", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("⚠️ Email already exists: {}", email);
            throw new BusinessException("User already exists", HttpStatus.CONFLICT);
        }
    }

    private String returnTrimmedEmail(String email){
        return email.trim();
    }

}