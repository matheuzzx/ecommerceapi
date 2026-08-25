package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.domain.Money;
import br.com.matheus.commerceapi.dto.request.product.CreateProductRequestDto;
import br.com.matheus.commerceapi.dto.request.product.UpdateProductRequestDto;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private ValidationUtils validationUtils;

    @Mock
    private CategoryService categoryService;

    @Mock
    private StoreService storeService;

    @InjectMocks
    private ProductService productService;

    private static final Long PRODUCT_ID = 1L;
    private static final String NAME = "Product Name";
    private static final String DESCRIPTION = "Product Description";
    private static final Money PRICE = Money.of(BigDecimal.valueOf(100));
    private static final Long CATEGORY_ID = 1L;
    private static final Long STORE_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Integer QUANTITY = 10;

    private Category createCategory() {
        return Category.builder().id(CATEGORY_ID).displayName("Category").build();
    }

    private Store createStore() {
        return Store.builder().id(STORE_ID).name("Store").build();
    }

    private Product createProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .price(PRICE)
                .active(true)
                .category(createCategory())
                .store(createStore())
                .build();
    }

    private CreateProductRequestDto createProductRequest() {
        return new CreateProductRequestDto(NAME, DESCRIPTION, PRICE, CATEGORY_ID, STORE_ID, QUANTITY);
    }

    private UpdateProductRequestDto createUpdateRequest() {
        return new UpdateProductRequestDto("Updated Name", "Updated Description", Money.of(BigDecimal.valueOf(200)), 2L);
    }

    // ============================================
    // CREATE PRODUCT TESTS
    // ============================================

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            CreateProductRequestDto request = createProductRequest();
            Category category = createCategory();
            Store store = createStore();
            Stock stock = Stock.builder().id(1L).product(null).quantity(QUANTITY).reserved(0).build();

            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryService.findActiveCategoryById(CATEGORY_ID)).thenReturn(category);
            when(storeService.findActiveStoreByOwner(STORE_ID, USER_ID)).thenReturn(store);
            when(productRepository.existsByNameAndStoreId(NAME, STORE_ID)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                product.setId(PRODUCT_ID);
                return product;
            });
            when(stockService.createStockForProduct(any(Product.class))).thenReturn(stock);
            when(stockService.addStock(anyLong(), anyInt())).thenReturn(stock);

            ProductResponseDto result = productService.createProduct(request, USER_ID);

            assertThat(result.id()).isEqualTo(PRODUCT_ID);
            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.price()).isEqualTo(PRICE);

            verify(productRepository, times(2)).save(any(Product.class));
            verify(stockService).createStockForProduct(any(Product.class));
            verify(stockService).addStock(anyLong(), anyInt());
        }

        @Test
        @DisplayName("Should throw exception when product name already exists in store")
        void shouldThrowExceptionWhenProductNameAlreadyExists() {
            CreateProductRequestDto request = createProductRequest();

            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryService.findActiveCategoryById(CATEGORY_ID)).thenReturn(createCategory());
            when(storeService.findActiveStoreByOwner(STORE_ID, USER_ID)).thenReturn(createStore());
            when(productRepository.existsByNameAndStoreId(NAME, STORE_ID)).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(request, USER_ID))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessageContaining(NAME);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            CreateProductRequestDto request = createProductRequest();

            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryService.findActiveCategoryById(CATEGORY_ID)).thenThrow(new NotFoundException("Category not found"));

            assertThatThrownBy(() -> productService.createProduct(request, USER_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            CreateProductRequestDto request = createProductRequest();

            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryService.findActiveCategoryById(CATEGORY_ID)).thenReturn(createCategory());
            when(storeService.findActiveStoreByOwner(STORE_ID, USER_ID)).thenThrow(new NotFoundException("Store not found"));

            assertThatThrownBy(() -> productService.createProduct(request, USER_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(productRepository, never()).save(any(Product.class));
        }
    }

    // ============================================
    // FIND PRODUCTS BY STORE OWNER TESTS
    // ============================================

    @Nested
    @DisplayName("Find Products By Store Owner Tests")
    class FindProductsByStoreOwnerTests {

        @Test
        @DisplayName("Should return page of products for store owner")
        void shouldReturnProductsForStoreOwner() {
            Store store = createStore();
            Product product = createProduct();
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> productPage = new PageImpl<>(List.of(product));

            when(storeService.findStoreByStoreOwner(USER_ID)).thenReturn(store);
            when(productRepository.findByStore(store, pageable)).thenReturn(productPage);

            Page<ProductResponseDto> result = productService.findProductsByStoreOwner(USER_ID, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(PRODUCT_ID);
        }
    }

    // ============================================
    // GET PRODUCT DETAILS BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("Get Product Details By ID Tests")
    class GetProductDetailsByIdTests {

        @Test
        @DisplayName("Should return product details when product exists")
        void shouldReturnProductDetailsWhenProductExists() {
            Product product = createProduct();
            Stock stock = Stock.builder().id(1L).product(product).quantity(10).reserved(0).build();
            product.setStock(stock);

            when(productRepository.findByIdAndStore_StoreOwnerId(PRODUCT_ID, USER_ID)).thenReturn(Optional.of(product));

            ProductDetailsResponseDto result = productService.getProductDetailsById(PRODUCT_ID, USER_ID);

            assertThat(result.id()).isEqualTo(PRODUCT_ID);
            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.stockQuantity()).isEqualTo(10);
            assertThat(result.inStock()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(productRepository.findByIdAndStore_StoreOwnerId(PRODUCT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductDetailsById(PRODUCT_ID, USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(PRODUCT_ID));
        }
    }

    // ============================================
    // UPDATE PRODUCT TESTS
    // ============================================

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update all fields successfully")
        void shouldUpdateAllFieldsSuccessfully() {
            Product existingProduct = createProduct();
            UpdateProductRequestDto request = createUpdateRequest();
            Category newCategory = Category.builder().id(2L).displayName("New Category").build();
            Product updatedProduct = createProduct();

            when(productRepository.findByIdAndStore_StoreOwnerId(PRODUCT_ID, USER_ID)).thenReturn(Optional.of(existingProduct));
            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryService.findActiveCategoryById(2L)).thenReturn(newCategory);
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            ProductDetailsResponseDto result = productService.updateProduct(PRODUCT_ID, request, USER_ID);

            assertThat(result.id()).isEqualTo(PRODUCT_ID);
            assertThat(existingProduct.getName()).isEqualTo("Updated Name");
            assertThat(existingProduct.getDescription()).isEqualTo("Updated Description");
            assertThat(existingProduct.getPrice()).isEqualTo(Money.of(BigDecimal.valueOf(200)));
            assertThat(existingProduct.getCategory().getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should partially update only provided fields")
        void shouldPartiallyUpdateOnlyProvidedFields() {
            Product existingProduct = createProduct();
            UpdateProductRequestDto request = new UpdateProductRequestDto(null, null, null, null);

            when(productRepository.findByIdAndStore_StoreOwnerId(PRODUCT_ID, USER_ID)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

            ProductDetailsResponseDto result = productService.updateProduct(PRODUCT_ID, request, USER_ID);

            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.description()).isEqualTo(DESCRIPTION);
            assertThat(result.price()).isEqualTo(PRICE);
            assertThat(existingProduct.getCategory().getId()).isEqualTo(CATEGORY_ID);

            verify(validationUtils, never()).validateRequiredString(any());
            verify(categoryService, never()).findActiveCategoryById(any());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(productRepository.findByIdAndStore_StoreOwnerId(PRODUCT_ID, USER_ID)).thenReturn(Optional.empty());

            UpdateProductRequestDto request = createUpdateRequest();

            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request, USER_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ============================================
    // DELETE PRODUCT TESTS
    // ============================================

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            Product product = createProduct();

            when(productRepository.findByIdAndStore_StoreOwnerId(PRODUCT_ID, USER_ID)).thenReturn(Optional.of(product));

            productService.deleteProduct(PRODUCT_ID, USER_ID);

            verify(productRepository).delete(product);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            when(productRepository.findByIdAndStore_StoreOwnerId(PRODUCT_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID, USER_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(productRepository, never()).delete(any(Product.class));
        }
    }

    // ============================================
    // FIND PRODUCT BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("Find Product By ID Tests")
    class FindProductByIdTests {

        @Test
        @DisplayName("Should return product when it exists")
        void shouldReturnProductWhenExists() {
            Product product = createProduct();

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            Product result = productService.findProductById(PRODUCT_ID);

            assertThat(result).isEqualTo(product);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenNotFound() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findProductById(PRODUCT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(PRODUCT_ID));
        }
    }
}
