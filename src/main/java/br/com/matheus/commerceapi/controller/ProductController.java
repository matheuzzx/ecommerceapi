package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
