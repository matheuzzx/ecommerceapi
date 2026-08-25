package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.order.CreateOrderRequestDto;
import br.com.matheus.commerceapi.dto.response.order.OrderResponseDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Orders", description = "Order placement and management for the authenticated CUSTOMER")
public interface OrderApi {

    @Operation(summary = "Create an order",
            description = "Creates an order for the authenticated customer, reserving stock for each item.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload, inactive product or insufficient stock",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Store or address not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> createOrder(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @RequestBody @Valid CreateOrderRequestDto request);

    @Operation(summary = "Get an order", description = "Returns one of the customer's orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> getOrder(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long orderId);

    @Operation(summary = "Cancel an order",
            description = "Cancels a CREATED order and releases its reservation, or refunds a PAID order and restores physical stock. SHIPPED and DELIVERED orders cannot be canceled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order canceled",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order cannot be canceled in its current status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> cancelOrder(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long orderId);

    @Operation(summary = "List my orders", description = "Returns all orders of the authenticated customer, paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders returned",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Page<OrderResponseDto>> getCustomerOrders(
            Pageable pageable,
            @Parameter(hidden = true) UserDetailsImpl userDetails);
}
