package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.request.product.UpdateProductRequestDto;
import br.com.matheus.commerceapi.dto.request.product.UpdateStockRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductDetailsResponseDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.dto.response.stock.StockResponseDto;
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

@Tag(name = "Store Products", description = "Product and stock management for the authenticated STOREOWNER")
public interface StoreProductApi {

    @Operation(summary = "Create a product",
            description = "Creates a product inside the authenticated owner's store. A stock record is created automatically.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Store or category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Name already used in the store",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ProductResponseDto> createProduct(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @RequestBody @Valid CreateProductRequestDto request);

    @Operation(summary = "List my products",
            description = "Returns the products of the authenticated owner's store, paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products returned",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Store not found for the authenticated owner",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Page<ProductResponseDto>> getProducts(
            @Parameter(hidden = true) UserDetailsImpl userDetails, Pageable pageable);

    @Operation(summary = "Get product details",
            description = "Returns full product details plus its stock information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details returned",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ProductDetailsResponseDto> getProductDetailsById(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long productId);

    @Operation(summary = "Update a product", description = "Updates the product fields of one of the owner's products.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Name already used in the store",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ProductDetailsResponseDto> updateProduct(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequestDto request);

    @Operation(summary = "Delete a product", description = "Deletes one of the owner's products.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> deleteProduct(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long productId);

    @Operation(summary = "Add stock", description = "Increases the available stock of one of the owner's products.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock updated",
                    content = @Content(schema = @Schema(implementation = StockResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<StockResponseDto> addStock(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateStockRequestDto request);

    @Operation(summary = "Remove stock", description = "Decreases the available stock of one of the owner's products.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock updated",
                    content = @Content(schema = @Schema(implementation = StockResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient stock",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<StockResponseDto> removeStock(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateStockRequestDto request);
}
