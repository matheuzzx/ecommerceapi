package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.user.UpdateUserRequestDto;
import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.findUserById(userDetails.getId());
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMe(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid UpdateUserRequestDto request) {

        UserResponseDto user = userService.updateUser(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
