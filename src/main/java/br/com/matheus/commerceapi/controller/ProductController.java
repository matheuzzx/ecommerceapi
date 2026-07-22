package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/myProducts")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("@storeSecurityService.isStoreOwner(#request.storeId(), #authentication.principal.id)")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody CreateProductRequestDto request, Authentication authentication) {
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
}
