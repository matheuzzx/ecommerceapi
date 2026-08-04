package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.request.product.UpdateProductRequestDto;
import br.com.matheus.commerceapi.dto.request.product.UpdateStockRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductDetailsResponseDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.dto.response.stock.StockResponseDto;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.ProductService;
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
@RequestMapping("/stores/my/products")
public class StoreProductController {

    private final ProductService productService;

    public StoreProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("@securityService.isStoreOwner(#request.storeId(), #userDetails.id)")
    public ResponseEntity<ProductResponseDto> createProduct(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreateProductRequestDto request) {

        ProductResponseDto product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping
    @PreAuthorize("hasRole('STOREOWNER')")
    public ResponseEntity<Page<ProductResponseDto>> getProducts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductResponseDto> products = productService.findProductsByStoreOwner(userDetails.getId(), pageable);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}/details")
    @PreAuthorize("hasRole('STOREOWNER') and @securityService.isProductOwner(#productId, #userDetails.id)")
    public ResponseEntity<ProductDetailsResponseDto> getProductDetailsById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId) {

        return ResponseEntity.ok(productService.getProductDetailsById(productId));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('STOREOWNER') and @securityService.isProductOwner(#productId, #userDetails.id)")
    public ResponseEntity<ProductDetailsResponseDto> updateProduct(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequestDto  request) {

        ProductDetailsResponseDto product = productService.updateProduct(productId, request);

        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('STOREOWNER') and @securityService.isProductOwner(#productId, #userDetails.id)")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{productId}/stock/add")
    @PreAuthorize("hasRole('STOREOWNER') and @securityService.isProductOwner(#productId, #userDetails.id)")
    public ResponseEntity<StockResponseDto> addStock(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateStockRequestDto request) {

        StockResponseDto stock = productService.addStock(productId, request.amount());
        return ResponseEntity.ok(stock);
    }

    @PutMapping("/{productId}/stock/remove")
    @PreAuthorize("hasRole('STOREOWNER') and @securityService.isProductOwner(#productId, #userDetails.id)")
    public ResponseEntity<StockResponseDto> removeStock(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateStockRequestDto request) {

        StockResponseDto stock = productService.removeStock(productId, request.amount());
        return ResponseEntity.ok(stock);
    }
}
