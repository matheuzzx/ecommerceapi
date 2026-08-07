package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.request.product.UpdateProductRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductDetailsResponseDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.dto.response.stock.StockResponseDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.exception.AlreadyExistsException;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
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
    public ProductResponseDto createProduct(CreateProductRequestDto request, Long userId) {

        validateCreateProductRequest(request);

        Category category = categoryService.findActiveCategoryById(request.categoryId());
        Store store = storeService.findActiveStoreByOwner(request.storeId(), userId);

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

    public ProductDetailsResponseDto getProductDetailsById(Long productId, Long userId) {
        Product product = findProductByOwner(productId, userId);
        return ProductDetailsResponseDto.fromEntity(product);
    }

    public ProductDetailsResponseDto updateProduct(Long productId, UpdateProductRequestDto request, Long userId) {
        Product product = findProductByOwner(productId, userId);

        if (request.name() != null) {
            validationUtils.validateRequiredString(Map.of("Name", request.name()));
            product.setName(request.name());
        }
        if (request.description() != null) {
            validationUtils.validateRequiredString(Map.of("Description", request.description()));
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            validatePrice(request.price());
            product.setPrice(request.price());
        }
        if (request.categoryId() != null) {
            Category category = categoryService.findActiveCategoryById(request.categoryId());
            product.setCategory(category);
        }

        productRepository.save(product);

        return ProductDetailsResponseDto.fromEntity(product);
    }

    public void deleteProduct(Long productId, Long userId) {
        Product product = findProductByOwner(productId, userId);
        productRepository.delete(product);
    }

    public StockResponseDto addStock(Long productId, Integer amount, Long userId) {
        findProductByOwner(productId, userId);
        return StockResponseDto.fromEntity(stockService.addStock(productId, amount));
    }

    public StockResponseDto removeStock(Long productId, Integer amount, Long userId) {
        findProductByOwner(productId, userId);
        return StockResponseDto.fromEntity(stockService.removeStock(productId, amount));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchActiveProducts(String name, Long categoryId, Long storeId,
                                                          BigDecimal minPrice, BigDecimal maxPrice,
                                                          Pageable pageable) {
        return productRepository.searchActiveProducts(name, categoryId, storeId, minPrice, maxPrice, pageable)
                .map(ProductResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductDetailsResponseDto getPublicProductDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> {
                    log.warn("Product not found or inactive: ID {}", productId);
                    return new NotFoundException("Product not found or inactive");
                });
        return ProductDetailsResponseDto.fromEntity(product);
    }

    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found: ID {}", productId);
                    return new NotFoundException("Product not found with id: " + productId);
                });
    }

    private Product findProductByOwner(Long productId, Long userId) {
        return productRepository.findByIdAndStore_StoreOwnerId(productId, userId)
                .orElseThrow(() -> {
                    log.warn("Product not found or not owned: ID {}, owner {}", productId, userId);
                    return new NotFoundException("Product not found with id: " + productId);
                });
    }

    private void validateCreateProductRequest(CreateProductRequestDto request) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Name", request.name());
        fields.put("Description", request.description());
        validationUtils.validateRequiredString(fields);

        validatePrice(request.price());
    }

    private void validateProductUniqueness(CreateProductRequestDto request) {
        if (productRepository.existsByNameAndStoreId(request.name(), request.storeId())) {
            log.warn("Product '{}' already exists in store {}", request.name(), request.storeId());
            throw new AlreadyExistsException("Product '" + request.name() + "' already exists in this store");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Invalid product price: {}", price);
            throw new InvalidArgumentException("Price must be greater than or equal to zero");
        }
    }
}