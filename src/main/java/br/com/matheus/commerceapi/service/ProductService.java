package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.exception.AlreadyExistsException;
import br.com.matheus.commerceapi.exception.StoreNotFoundException;
import br.com.matheus.commerceapi.repository.CategoryRepository;
import br.com.matheus.commerceapi.repository.ProductRepository;
import br.com.matheus.commerceapi.repository.StoreRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ValidationUtils validationUtils;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;


    public Product createProduct(CreateProductRequestDto request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(StoreNotFoundException::new);

        Map<String, String> fields = new HashMap<>();
        fields.put("Name", request.name());
        fields.put("SKU", request.sku());
        fields.put("Description", request.description());

        validationUtils.validateRequiredString(fields);

        validatePrice(request.price());

        if(!category.isActive()){
            throw new IllegalStateException("Category is not active");
        }

        if(!store.isActive()){
            throw new IllegalStateException("Store is not active");
        }

        if (productRepository.existsByNameAndStoreId(request.name(), request.storeId())) {
            throw new AlreadyExistsException("Product '" + request.name() + "' already exists in this store");
        }

        Stock stock = createInitialStock(request.quantity());

        Product product = Product.builder()
                .name(request.name())
                .sku(request.sku())
                .description(request.description())
                .price(request.price())
                .active(true)
                .category(category)
                .store(store)
                .stock(stock)
                .build();

        return productRepository.save(product);
    }

    private Stock createInitialStock(Integer quantity) {

        if(quantity <= 0){
            throw new IllegalStateException("Quantity must be greater than zero");
        }

        return Stock.builder()
                .quantity(quantity)
                .reserved(0)
                .build();
    }

    private void validatePrice(BigDecimal price) {
        if(price == null || price.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Price must be greater than or equal to zero");
        }
    }

}
