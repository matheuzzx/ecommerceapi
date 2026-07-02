package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.auth.LoginRequestDto;
import br.com.matheus.commerceapi.dto.request.auth.RegisterUserRequestDto;
import br.com.matheus.commerceapi.dto.response.auth.TokenResponseDto;
import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.exception.EmailAlreadyExistsException;
import br.com.matheus.commerceapi.exception.InvalidCredentialsException;
import br.com.matheus.commerceapi.exception.InvalidRoleException;
import br.com.matheus.commerceapi.exception.UserNotFoundException;
import br.com.matheus.commerceapi.repository.UserRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ValidationUtils validationUtils;

    @InjectMocks
    private AuthService authService;

    private static final String NAME = "name";
    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "password";
    private static final String HASHED = "hashed";
    private static final String TOKEN = "token";
    private static final Long ID = 1L;

    // ============================================
    // REGISTER TESTS
    // ============================================

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        // ===== SUCCESS SCENARIOS =====

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @ParameterizedTest
            @ValueSource(strings = {"CUSTOMER", "STOREOWNER"})
            @DisplayName("Should register user with valid role")
            void shouldRegisterUserWithValidRole(String role) {
                // Arrange
                RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, EMAIL, PASSWORD, role);
                User savedUser = createUser(role);

                mockValidationPass();
                when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED);
                when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                // Act
                UserResponseDto response = authService.register(request);

                // Assert
                assertResponse(response, role);
            }

            @Test
            @DisplayName("Should always set store as null when registering a new user")
            void shouldAlwaysSetStoreAsNullOnRegistration() {
                // Arrange
                RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, EMAIL, PASSWORD, "CUSTOMER");
                User savedUser = createUser("CUSTOMER");

                mockValidationPass();
                when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED);
                when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                // Act
                authService.register(request);

                // Assert
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());
                assertThat(userCaptor.getValue().getStore()).isNull();
            }
        }

        // ===== VALIDATION TESTS =====

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {

            @Test
            @DisplayName("Should throw exception when email format is invalid")
            void shouldThrowExceptionWhenEmailIsInvalid() {
                // Arrange
                String invalidEmail = "invalid_email";
                RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, invalidEmail, PASSWORD, "CUSTOMER");

                doNothing().when(validationUtils).validateRequired(any());
                doThrow(new IllegalArgumentException("Email is not valid"))
                        .when(validationUtils).validateEmailFormat(invalidEmail);

                // Act & Assert
                assertThatThrownBy(() -> authService.register(request))
                        .isInstanceOf(IllegalArgumentException.class);

                verify(userRepository, never()).save(any(User.class));
                verify(passwordEncoder, never()).encode(anyString());
            }

            @ParameterizedTest
            @MethodSource("invalidRegisterFieldsProvider")
            @DisplayName("Should throw exception when any required field is invalid")
            void shouldThrowExceptionWhenRequiredFieldIsInvalid(String fieldName, String invalidValue) {
                // Arrange
                RegisterUserRequestDto request = new RegisterUserRequestDto(
                        fieldName.equals("name") ? invalidValue : NAME,
                        fieldName.equals("email") ? invalidValue : EMAIL,
                        fieldName.equals("password") ? invalidValue : PASSWORD,
                        "CUSTOMER"
                );

                doThrow(new IllegalArgumentException("Field cannot be null or blank"))
                        .when(validationUtils).validateRequired(any());

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> authService.register(request));

                verifyNoInteractionsWithRepository();
            }

            private static Stream<Arguments> invalidRegisterFieldsProvider() {
                return Stream.of(
                        Arguments.of("name", null),
                        Arguments.of("name", ""),
                        Arguments.of("name", "   "),
                        Arguments.of("name", "\t"),
                        Arguments.of("email", null),
                        Arguments.of("email", ""),
                        Arguments.of("email", "   "),
                        Arguments.of("email", "\t"),
                        Arguments.of("password", null),
                        Arguments.of("password", ""),
                        Arguments.of("password", "   "),
                        Arguments.of("password", "\t")
                );
            }
        }

        // ===== EXCEPTION TESTS =====

        @Nested
        @DisplayName("Exception Tests")
        class ExceptionTests {

            @Test
            @DisplayName("Should throw exception when role is ADMIN")
            void shouldThrowExceptionWhenRoleIsAdmin() {
                // Arrange
                RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, EMAIL, PASSWORD, "ADMIN");

                mockValidationPass();

                // Act & Assert
                assertThatThrownBy(() -> authService.register(request))
                        .isInstanceOf(InvalidRoleException.class);

                verify(userRepository, never()).save(any(User.class));
                verify(passwordEncoder, never()).encode(anyString());
            }

            @Test
            @DisplayName("Should throw exception when email already exists")
            void shouldThrowExceptionWhenEmailAlreadyExists() {
                // Arrange
                RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, EMAIL, PASSWORD, "CUSTOMER");

                mockValidationPass();
                when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> authService.register(request))
                        .isInstanceOf(EmailAlreadyExistsException.class);

                verify(userRepository, never()).save(any(User.class));
                verify(passwordEncoder, never()).encode(anyString());
            }
        }
    }

    // ============================================
    // LOGIN TESTS
    // ============================================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        // ===== SUCCESS SCENARIOS =====

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should login successfully")
            void shouldLoginSuccessfully() {
                // Arrange
                LoginRequestDto request = new LoginRequestDto(EMAIL, PASSWORD);
                User user = createUser("CUSTOMER");
                
                when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
                when(passwordEncoder.matches(PASSWORD, HASHED)).thenReturn(true);
                when(jwtService.generateToken(EMAIL, "CUSTOMER")).thenReturn(TOKEN);

                // Act
                TokenResponseDto response = authService.login(request);

                // Assert
                assertThat(response.token()).isEqualTo(TOKEN);
            }
        }

        // ===== VALIDATION TESTS =====

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {

            @ParameterizedTest
            @MethodSource("invalidLoginFieldsProvider")
            @DisplayName("Should throw exception when login fields are invalid")
            void shouldThrowExceptionWhenLoginFieldIsInvalid(String fieldName, String invalidValue) {
                // Arrange
                LoginRequestDto request = new LoginRequestDto(
                        fieldName.equals("email") ? invalidValue : EMAIL,
                        fieldName.equals("password") ? invalidValue : PASSWORD
                );

                doThrow(new IllegalArgumentException("Field cannot be null or blank"))
                        .when(validationUtils).validateRequired(any());

                // Act & Assert
                assertThrows(IllegalArgumentException.class, () -> authService.login(request));

                verify(userRepository, never()).findByEmail(anyString());
                verify(passwordEncoder, never()).matches(anyString(), anyString());
            }

            private static Stream<Arguments> invalidLoginFieldsProvider() {
                return Stream.of(
                        Arguments.of("email", null),
                        Arguments.of("email", ""),
                        Arguments.of("email", "   "),
                        Arguments.of("email", "\t"),
                        Arguments.of("password", null),
                        Arguments.of("password", ""),
                        Arguments.of("password", "   "),
                        Arguments.of("password", "\t")
                );
            }
        }

        // ===== EXCEPTION TESTS =====

        @Nested
        @DisplayName("Exception Tests")
        class ExceptionTests {

            @Test
            @DisplayName("Should throw exception when user not found")
            void shouldThrowExceptionWhenUserNotFound() {
                // Arrange
                LoginRequestDto request = new LoginRequestDto(EMAIL, PASSWORD);

                when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> authService.login(request))
                        .isInstanceOf(UserNotFoundException.class);

                verify(passwordEncoder, never()).matches(anyString(), anyString());
            }

            @Test
            @DisplayName("Should throw exception when password is wrong")
            void shouldThrowExceptionWhenPasswordIsWrong() {
                // Arrange
                LoginRequestDto request = new LoginRequestDto(EMAIL, "wrong");
                User user = createUser("CUSTOMER");

                when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
                when(passwordEncoder.matches("wrong", HASHED)).thenReturn(false);

                // Act & Assert
                assertThatThrownBy(() -> authService.login(request))
                        .isInstanceOf(InvalidCredentialsException.class);
            }
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private User createUser(String role) {
        return User.builder()
                .id(ID)
                .name(NAME)
                .email(EMAIL)
                .passwordHash(HASHED)
                .userRole(UserRole.valueOf(role))
                .store(null)
                .build();
    }

    private void mockValidationPass() {
        doNothing().when(validationUtils).validateRequired(any());
        doNothing().when(validationUtils).validateEmailFormat(any());
    }

    private void assertResponse(UserResponseDto response, String expectedRole) {
        assertThat(response.id()).isEqualTo(ID);
        assertThat(response.name()).isEqualTo(NAME);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.role()).isEqualTo(expectedRole);
    }

    private void verifyNoInteractionsWithRepository() {
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }
}