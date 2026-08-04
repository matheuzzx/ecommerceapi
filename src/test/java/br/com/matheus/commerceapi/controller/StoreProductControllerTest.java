package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.product.UpdateProductRequestDto;
import br.com.matheus.commerceapi.dto.response.product.ProductDetailsResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreProductController Tests")
class StoreProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private StoreProductController productController;

    private static final Long PRODUCT_ID = 1L;
    private static final Long USER_ID = 1L;

    private UserDetailsImpl createUserDetails() {
        User user = User.builder().id(USER_ID).build();
        return new UserDetailsImpl(user);
    }

    // ============================================
    // GET PRODUCT DETAILS TESTS
    // ============================================

    @Nested
    @DisplayName("Get Product Details Tests")
    class GetProductDetailsTests {

        @Test
        @DisplayName("Should return product details with 200")
        void shouldReturnProductDetails() {
            UserDetailsImpl userDetails = createUserDetails();
            ProductDetailsResponseDto response = new ProductDetailsResponseDto(
                    PRODUCT_ID, "Name", "Desc", BigDecimal.TEN, true, null, null, 10, true, null, null);

            when(productService.getProductDetailsById(PRODUCT_ID)).thenReturn(response);

            ResponseEntity<ProductDetailsResponseDto> result = productController.getProductDetailsById(userDetails, PRODUCT_ID);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(response);
        }

        @Test
        @DisplayName("Should propagate 404 when product not found")
        void shouldPropagateNotFound() {
            UserDetailsImpl userDetails = createUserDetails();

            when(productService.getProductDetailsById(PRODUCT_ID)).thenThrow(new NotFoundException("Product not found with id: " + PRODUCT_ID));

            assertThatThrownBy(() -> productController.getProductDetailsById(userDetails, PRODUCT_ID))
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
        @DisplayName("Should update product and return 200")
        void shouldUpdateProduct() {
            UserDetailsImpl userDetails = createUserDetails();
            UpdateProductRequestDto request = new UpdateProductRequestDto("New Name", null, null, null);
            ProductDetailsResponseDto response = new ProductDetailsResponseDto(
                    PRODUCT_ID, "New Name", "Desc", BigDecimal.TEN, true, null, null, 10, true, null, null);

            when(productService.updateProduct(PRODUCT_ID, request)).thenReturn(response);

            ResponseEntity<ProductDetailsResponseDto> result = productController.updateProduct(userDetails, PRODUCT_ID, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(response);
        }

        @Test
        @DisplayName("Should propagate 404 when product not found")
        void shouldPropagateNotFound() {
            UserDetailsImpl userDetails = createUserDetails();
            UpdateProductRequestDto request = new UpdateProductRequestDto("Name", null, null, null);

            when(productService.updateProduct(PRODUCT_ID, request)).thenThrow(new NotFoundException("Product not found with id: " + PRODUCT_ID));

            assertThatThrownBy(() -> productController.updateProduct(userDetails, PRODUCT_ID, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(PRODUCT_ID));
        }
    }
}
