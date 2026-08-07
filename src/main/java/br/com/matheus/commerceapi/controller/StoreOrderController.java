package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.OrderService;
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
@RequestMapping("/stores/my/orders")
public class StoreOrderController {

    private final OrderService orderService;

    public StoreOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasRole('STOREOWNER')")
    public ResponseEntity<Page<OrderResponseDto>> getStoreOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Page<OrderResponseDto> orders = orderService.getStoreOwnerOrders(userDetails.getId(), pageable);

        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @PutMapping("/{orderId}/ship")
    @PreAuthorize("hasRole('STOREOWNER')")
    public ResponseEntity<OrderResponseDto> shipOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long orderId) {

        OrderResponseDto order = orderService.shipOrderForStoreOwner(orderId, userDetails.getId());
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('STOREOWNER')")
    public ResponseEntity<OrderResponseDto> deliverOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long orderId) {

        OrderResponseDto order = orderService.deliverOrderForStoreOwner(orderId, userDetails.getId());
        return ResponseEntity.ok(order);
    }
}
