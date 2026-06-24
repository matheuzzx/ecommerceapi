package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.LoginRequestDto;
import br.com.matheus.commerceapi.dto.request.RegisterUserRequestDto;
import br.com.matheus.commerceapi.dto.response.TokenResponseDto;
import br.com.matheus.commerceapi.dto.response.UserResponseDto;
import br.com.matheus.commerceapi.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody RegisterUserRequestDto request){

        UserResponseDto user = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto request){

        TokenResponseDto token = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

}
