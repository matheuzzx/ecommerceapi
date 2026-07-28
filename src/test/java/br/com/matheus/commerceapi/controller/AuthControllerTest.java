package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.auth.LoginRequestDto;
import br.com.matheus.commerceapi.dto.request.auth.RegisterUserRequestDto;
import br.com.matheus.commerceapi.dto.response.auth.TokenResponseDto;
import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.exception.EmailAlreadyExistsException;
import br.com.matheus.commerceapi.exception.InvalidCredentialsException;
import br.com.matheus.commerceapi.exception.InvalidRoleException;
import br.com.matheus.commerceapi.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private static final String NAME = "User";
    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "password";
    private static final String ROLE = "CUSTOMER";
    private static final String TOKEN = "jwt.token.here";

    // ============================================
    // REGISTER TESTS
    // ============================================

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user and return 201")
        void shouldRegisterUserAndReturnCreated() {
            RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, EMAIL, PASSWORD, ROLE);
            UserResponseDto responseDto = new UserResponseDto(1L, NAME, EMAIL, ROLE);

            when(authService.register(request)).thenReturn(responseDto);

            ResponseEntity<UserResponseDto> result = authController.register(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(responseDto);
        }

        @Test
        @DisplayName("Should propagate exception when role is invalid")
        void shouldPropagateExceptionWhenRoleIsInvalid() {
            RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, EMAIL, PASSWORD, "ADMIN");

            when(authService.register(request)).thenThrow(new InvalidRoleException("Invalid role"));

            assertThatThrownBy(() -> authController.register(request))
                    .isInstanceOf(InvalidRoleException.class);
        }

        @Test
        @DisplayName("Should propagate exception when email already exists")
        void shouldPropagateExceptionWhenEmailAlreadyExists() {
            RegisterUserRequestDto request = new RegisterUserRequestDto(NAME, EMAIL, PASSWORD, ROLE);

            when(authService.register(request)).thenThrow(new EmailAlreadyExistsException(EMAIL));

            assertThatThrownBy(() -> authController.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class);
        }
    }

    // ============================================
    // LOGIN TESTS
    // ============================================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login user and return 200 with token")
        void shouldLoginUserAndReturnOk() {
            LoginRequestDto request = new LoginRequestDto(EMAIL, PASSWORD);
            TokenResponseDto responseDto = new TokenResponseDto(TOKEN);

            when(authService.login(request)).thenReturn(responseDto);

            ResponseEntity<TokenResponseDto> result = authController.login(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(responseDto);
        }

        @Test
        @DisplayName("Should propagate exception when credentials are invalid")
        void shouldPropagateExceptionWhenInvalidCredentials() {
            LoginRequestDto request = new LoginRequestDto(EMAIL, "wrong");

            when(authService.login(request)).thenThrow(new InvalidCredentialsException());

            assertThatThrownBy(() -> authController.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
