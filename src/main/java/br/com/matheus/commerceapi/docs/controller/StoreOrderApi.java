package br.com.matheus.commerceapi.docs.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Store Orders", description = "Order fulfillment for the authenticated STOREOWNER")
public interface StoreOrderApi {

    @Operation(summary = "List my store orders",
            description = "Returns all orders placed in the authenticated owner's store, paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders returned",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Store not found for the authenticated owner",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Page<OrderResponseDto>> getStoreOrders(
            Pageable pageable,
            @Parameter(hidden = true) UserDetailsImpl userDetails);

    @Operation(summary = "Ship an order",
            description = "Moves an order from PAID to SHIPPED. Only orders of the owner's store.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order shipped",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order is not PAID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> shipOrder(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long orderId);

    @Operation(summary = "Deliver an order",
            description = "Moves an order from SHIPPED to DELIVERED. Only orders of the owner's store.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order delivered",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order is not SHIPPED",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<OrderResponseDto> deliverOrder(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long orderId);
}
