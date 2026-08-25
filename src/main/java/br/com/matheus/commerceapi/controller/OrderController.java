package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.docs.controller.OrderApi;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.OrderService;
import br.com.matheus.commerceapi.service.OrderCancellationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final OrderCancellationService orderCancellationService;

    public OrderController(OrderService orderService, OrderCancellationService orderCancellationService) {
        this.orderService = orderService;
        this.orderCancellationService = orderCancellationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreateOrderRequestDto request) {

        OrderResponseDto order = orderService.createOrder(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDto> getOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long orderId) {

        OrderResponseDto order = orderService.getOrder(orderId, userDetails.getId());

        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long orderId) {

        OrderResponseDto order = orderCancellationService.cancelOrder(orderId, userDetails.getId());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponseDto>> getCustomerOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Page<OrderResponseDto> orders = orderService.getCustomerOrders(userDetails.getId(), pageable);

        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }
}
