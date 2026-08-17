package br.com.matheus.commerceapi.handler;

import br.com.matheus.commerceapi.exception.AlreadyExistsException;
import br.com.matheus.commerceapi.exception.ConflictException;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import br.com.matheus.commerceapi.exception.InvalidCredentialsException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/products/1");

    @Test
    @DisplayName("Should return 500 for unexpected RuntimeException")
    void shouldReturn500ForUnexpectedRuntimeException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(new RuntimeException("boom"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
        assertThat(response.getBody().getPath()).isEqualTo("/products/1");
    }

    @Test
    @DisplayName("Should return 500 for unexpected checked exception")
    void shouldReturn500ForUnexpectedException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(new Exception("boom"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return 409 for data integrity violation (FK/unique constraint)")
    void shouldReturn409ForDataIntegrityViolation() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("constraint violation"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Data integrity violation");
    }

    @Test
    @DisplayName("Should return 404 for NotFoundException")
    void shouldReturn404ForNotFound() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new NotFoundException("not found"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 409 for ConflictException")
    void shouldReturn409ForConflict() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflict(new ConflictException("conflict"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should return 400 for InvalidArgumentException")
    void shouldReturn400ForInvalidArgument() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidArgument(new InvalidArgumentException("bad"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 409 for AlreadyExistsException")
    void shouldReturn409ForAlreadyExists() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAlreadyExists(new AlreadyExistsException("duplicate"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should return 401 for InvalidCredentialsException")
    void shouldReturn401ForInvalidCredentials() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException(), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return 401 for AuthenticationException")
    void shouldReturn401ForAuthenticationException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAuthenticationException(new BadCredentialsException("bad creds"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return 403 for AccessDeniedException")
    void shouldReturn403ForAccessDenied() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should return 400 for IllegalArgumentException")
    void shouldReturn400ForIllegalArgument() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad"), servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 400 with field errors for validation failure")
    void shouldReturn400WithFieldErrorsForValidationFailure() throws Exception {
        MethodParameter parameter =
                new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("target", String.class), 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "dto");
        bindingResult.addError(new FieldError("dto", "name", "Name is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        WebRequest webRequest = new ServletWebRequest(servletRequest);
        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                ex, new HttpHeaders(), HttpStatusCode.valueOf(400), webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((ErrorResponse) response.getBody()).getErrors()).containsExactly("Name is required");
    }

    @SuppressWarnings("unused")
    public void target(@NotBlank String name) {
    }
}
