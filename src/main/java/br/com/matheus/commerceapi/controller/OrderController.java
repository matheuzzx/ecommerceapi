package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreateOrderRequestDto request) {

        OrderResponseDto order = orderService.createOrder(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
