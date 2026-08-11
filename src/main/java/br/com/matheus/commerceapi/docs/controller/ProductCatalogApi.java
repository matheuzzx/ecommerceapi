package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.response.product.ProductDetailsResponseDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Tag(name = "Product Catalog", description = "Public endpoints to browse the catalog of active products")
public interface ProductCatalogApi {

    @Operation(summary = "List products",
            description = "Searches active products with optional filters for name, category, store and price range. Paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products returned",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter values",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    ResponseEntity<Page<ProductResponseDto>> listProducts(
            @Parameter(description = "Product name (partial match)") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Filter by category id") @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "Filter by store id") @RequestParam(value = "storeId", required = false) Long storeId,
            @Parameter(description = "Minimum price") @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            Pageable pageable);

    @Operation(summary = "Get public product details",
            description = "Returns the full details of an active product, including stock availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details returned",
                    content = @Content(schema = @Schema(implementation = ProductDetailsResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found or not active",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirements
    ResponseEntity<ProductDetailsResponseDto> getProductDetails(@PathVariable Long productId);
}
