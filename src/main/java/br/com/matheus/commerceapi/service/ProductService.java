package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductResponseDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.exception.AlreadyExistsException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.exception.StoreNotFoundException;
import br.com.matheus.commerceapi.repository.CategoryRepository;
import br.com.matheus.commerceapi.repository.ProductRepository;
import br.com.matheus.commerceapi.repository.StoreRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final StockService stockService;
    private final ValidationUtils validationUtils;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public ProductResponseDto createProduct(CreateProductRequestDto request) {

        validateRequest(request);

        Category category = findCategoryById(request.categoryId());
        Store store = findStoreById(request.storeId());

        validateCategoryAndStore(category, store);
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

        Stock stock = createInitialStock(savedProduct);
        savedProduct.setStock(stock);

        stockService.addStock(product.getId(), request.quantity());

        productRepository.save(savedProduct);

        return ProductResponseDto.fromEntity(savedProduct);
    }

    public List<ProductResponseDto> findProductsByUserId(Long userId) {
        Store store = storeRepository.findByStoreOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("Store not found for user: " + userId));

        List<Product> products = productRepository.findByStore(store);

        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateRequest(CreateProductRequestDto request) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Name", request.name());
        fields.put("Description", request.description());
        validationUtils.validateRequiredString(fields);

        validatePrice(request.price());
    }

    private void validateCategoryAndStore(Category category, Store store) {
        if (!category.isActive()) {
            throw new IllegalStateException("Category is not active");
        }

        if (!store.isActive()) {
            throw new IllegalStateException("Store is not active");
        }
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

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);
    }

    private Stock createInitialStock(Product product) {
        return stockService.createStockForProduct(product);
    }
}