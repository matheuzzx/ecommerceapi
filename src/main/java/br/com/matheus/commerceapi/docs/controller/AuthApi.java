package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.auth.LoginRequestDto;
import br.com.matheus.commerceapi.dto.request.auth.RegisterUserRequestDto;
import br.com.matheus.commerceapi.dto.response.auth.TokenResponseDto;
import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "Public endpoints for user registration and login")
public interface AuthApi {

    @Operation(summary = "Register a new user",
            description = "Creates an account with CUSTOMER or STOREOWNER role. The password is stored hashed.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload, email format or role",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    ResponseEntity<UserResponseDto> register(@RequestBody @Valid RegisterUserRequestDto request);

    @Operation(summary = "Login", description = "Authenticates the user with email and password and returns a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT token issued",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Missing required fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Email not registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginRequestDto request);
}
