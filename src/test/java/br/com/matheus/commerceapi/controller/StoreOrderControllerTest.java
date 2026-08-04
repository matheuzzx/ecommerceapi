package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreOrderController Tests")
class StoreOrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private StoreOrderController storeOrderController;

    private static final Long USER_ID = 1L;

    private UserDetailsImpl createUserDetails() {
        User user = User.builder().id(USER_ID).build();
        return new UserDetailsImpl(user);
    }

    @Test
    @DisplayName("Should return store orders page with 200")
    void shouldReturnStoreOrders() {
        UserDetailsImpl userDetails = createUserDetails();
        PageRequest pageable = PageRequest.of(0, 20);
        Page<OrderResponseDto> page = new PageImpl<>(List.of());

        when(orderService.getStoreOwnerOrders(eq(USER_ID), eq(pageable))).thenReturn(page);

        ResponseEntity<Page<OrderResponseDto>> result = storeOrderController.getStoreOrders(pageable, userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(page);
        verify(orderService).getStoreOwnerOrders(USER_ID, pageable);
    }
}
