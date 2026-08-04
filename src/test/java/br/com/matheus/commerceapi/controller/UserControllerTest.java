package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.user.UpdateUserRequestDto;
import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.exception.UserNotFoundException;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private static final Long USER_ID = 1L;

    private UserDetailsImpl createUserDetails() {
        User user = User.builder().id(USER_ID).build();
        return new UserDetailsImpl(user);
    }

    @Test
    @DisplayName("Should return current user with 200")
    void shouldReturnCurrentUser() {
        UserDetailsImpl userDetails = createUserDetails();
        User user = User.builder()
                .id(USER_ID)
                .name("John")
                .email("john@example.com")
                .userRole(UserRole.CUSTOMER)
                .build();

        when(userService.findUserById(USER_ID)).thenReturn(user);

        ResponseEntity<UserResponseDto> result = userController.getMe(userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(UserResponseDto.fromEntity(user));
        verify(userService).findUserById(USER_ID);
    }

    @Test
    @DisplayName("Should propagate 404 when user not found")
    void shouldPropagateNotFound() {
        UserDetailsImpl userDetails = createUserDetails();

        when(userService.findUserById(USER_ID)).thenThrow(new UserNotFoundException());

        assertThatThrownBy(() -> userController.getMe(userDetails))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should update current user and return 200")
    void shouldUpdateCurrentUser() {
        UserDetailsImpl userDetails = createUserDetails();
        UpdateUserRequestDto request = new UpdateUserRequestDto("New Name");
        User updatedUser = User.builder()
                .id(USER_ID)
                .name("New Name")
                .email("john@example.com")
                .userRole(UserRole.CUSTOMER)
                .build();

        when(userService.updateUser(USER_ID, request)).thenReturn(UserResponseDto.fromEntity(updatedUser));

        ResponseEntity<UserResponseDto> result = userController.updateMe(userDetails, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().name()).isEqualTo("New Name");
        verify(userService).updateUser(USER_ID, request);
    }
}
