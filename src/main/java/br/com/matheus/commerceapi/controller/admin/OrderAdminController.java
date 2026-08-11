package br.com.matheus.commerceapi.controller.admin;

import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.docs.controller.OrderAdminApi;
import br.com.matheus.commerceapi.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
public class OrderAdminController implements OrderAdminApi {

    private final OrderService orderService;

    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> confirmOrder(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> shipOrder(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.shipOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> deliverOrder(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.deliverOrder(orderId);
        return ResponseEntity.ok(order);
    }
}
