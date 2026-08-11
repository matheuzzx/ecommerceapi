package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Admin - Orders", description = "Order lifecycle management. Restricted to ADMIN.")
public interface OrderAdminApi {

    @Operation(summary = "Confirm an order",
            description = "Moves an order from CREATED to PAID and confirms the stock reservations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order confirmed",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order is not CREATED",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> confirmOrder(@PathVariable Long orderId);

    @Operation(summary = "Ship an order", description = "Moves an order from PAID to SHIPPED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order shipped",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order is not PAID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> shipOrder(@PathVariable Long orderId);

    @Operation(summary = "Deliver an order", description = "Moves an order from SHIPPED to DELIVERED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order delivered",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order is not SHIPPED",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> deliverOrder(@PathVariable Long orderId);
}
