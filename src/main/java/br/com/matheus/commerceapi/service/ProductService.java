package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductDetailsResponseDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.exception.AlreadyExistsException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.ProductRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StockService stockService;
    private final ValidationUtils validationUtils;
    private final CategoryService categoryService;
    private final StoreService storeService;

    @Transactional
    public ProductResponseDto createProduct(CreateProductRequestDto request) {

        validateRequest(request);

        Category category = categoryService.findActiveCategoryById(request.categoryId());
        Store store = storeService.findActiveStoreById(request.storeId());

        validateProductUniqueness(request);

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .active(true)
                .category(category)
                .store(store)
                .build();

        Product savedProduct = productRepository.save(product);

        Stock stock = stockService.createStockForProduct(savedProduct);
        savedProduct.setStock(stock);

        stockService.addStock(product.getId(), request.quantity());

        productRepository.save(savedProduct);

        return ProductResponseDto.fromEntity(savedProduct);
    }

    public Page<ProductResponseDto> findProductsByStoreOwner(Long userId, Pageable pageable) {
        Store store = storeService.findStoreByStoreOwner(userId);
        Page<Product> products = productRepository.findByStore(store, pageable);
        return products.map(ProductResponseDto::fromEntity);
    }

    public ProductDetailsResponseDto getProductDetailsById(Long productId) {
        Product product = findProductById(productId);
        return ProductDetailsResponseDto.fromEntity(product);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));
    }

    private void validateRequest(CreateProductRequestDto request) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Name", request.name());
        fields.put("Description", request.description());
        validationUtils.validateRequiredString(fields);

        validatePrice(request.price());
    }

    private void validateProductUniqueness(CreateProductRequestDto request) {
        if (productRepository.existsByNameAndStoreId(request.name(), request.storeId())) {
            throw new AlreadyExistsException("Product '" + request.name() + "' already exists in this store");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to zero");
        }
    }
}