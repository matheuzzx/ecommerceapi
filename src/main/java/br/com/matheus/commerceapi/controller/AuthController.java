package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.LoginRequestDto;
import br.com.matheus.commerceapi.dto.RegisterUserRequestDto;
import br.com.matheus.commerceapi.entity.User;
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
    public ResponseEntity<User> register(@RequestBody RegisterUserRequestDto request){

        User user = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request){

        String token = authService.login(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

}
