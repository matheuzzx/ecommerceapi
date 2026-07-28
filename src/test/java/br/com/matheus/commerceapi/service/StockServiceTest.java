package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Tests")
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    private static final Long PRODUCT_ID = 1L;
    private static final Integer AMOUNT = 5;
    private static final Integer STOCK_QUANTITY = 10;

    private Product createProduct() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        return product;
    }

    private Stock createStock(Integer quantity, Integer reserved) {
        return Stock.builder()
                .id(1L)
                .product(createProduct())
                .quantity(quantity)
                .reserved(reserved)
                .build();
    }

    private Stock createStock() {
        return createStock(STOCK_QUANTITY, 0);
    }

    // ============================================
    // CREATE STOCK FOR PRODUCT TESTS
    // ============================================

    @Nested
    @DisplayName("Create Stock For Product Tests")
    class CreateStockForProductTests {

        @Test
        @DisplayName("Should create stock with zero quantity and reserved")
        void shouldCreateStockWithZeroQuantity() {
            Product product = createProduct();
            Stock savedStock = Stock.builder()
                    .id(1L)
                    .product(product)
                    .quantity(0)
                    .reserved(0)
                    .build();

            when(stockRepository.save(any(Stock.class))).thenReturn(savedStock);

            Stock result = stockService.createStockForProduct(product);

            assertThat(result.getQuantity()).isZero();
            assertThat(result.getReserved()).isZero();
            assertThat(result.getProduct()).isEqualTo(product);

            ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
            verify(stockRepository).save(captor.capture());
            assertThat(captor.getValue().getProduct()).isEqualTo(product);
            assertThat(captor.getValue().getQuantity()).isZero();
            assertThat(captor.getValue().getReserved()).isZero();
        }
    }

    // ============================================
    // GET STOCK BY PRODUCT ID TESTS
    // ============================================

    @Nested
    @DisplayName("Get Stock By Product ID Tests")
    class GetStockByProductIdTests {

        @Test
        @DisplayName("Should return stock when product exists")
        void shouldReturnStockWhenProductExists() {
            Stock stock = createStock();

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            Stock result = stockService.getStockByProductId(PRODUCT_ID);

            assertThat(result).isEqualTo(stock);
        }

        @Test
        @DisplayName("Should throw exception when stock not found")
        void shouldThrowExceptionWhenStockNotFound() {
            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.getStockByProductId(PRODUCT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Stock Not Found");
        }
    }

    // ============================================
    // ADD STOCK TESTS
    // ============================================

    @Nested
    @DisplayName("Add Stock Tests")
    class AddStockTests {

        @Test
        @DisplayName("Should add stock successfully")
        void shouldAddStockSuccessfully() {
            Stock stock = createStock(STOCK_QUANTITY, 0);
            Stock stockAfterAdd = createStock(STOCK_QUANTITY + AMOUNT, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stockAfterAdd);

            Stock result = stockService.addStock(PRODUCT_ID, AMOUNT);

            assertThat(result.getQuantity()).isEqualTo(STOCK_QUANTITY + AMOUNT);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should throw exception when amount is invalid")
        void shouldThrowExceptionWhenAmountIsInvalid(Integer invalidAmount) {
            Stock stock = createStock();

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.addStock(PRODUCT_ID, invalidAmount))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(stockRepository, never()).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            Stock stock = createStock();

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.addStock(PRODUCT_ID, null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(stockRepository, never()).save(any(Stock.class));
        }
    }

    // ============================================
    // REMOVE STOCK TESTS
    // ============================================

    @Nested
    @DisplayName("Remove Stock Tests")
    class RemoveStockTests {

        @Test
        @DisplayName("Should remove stock successfully")
        void shouldRemoveStockSuccessfully() {
            Stock stock = createStock(STOCK_QUANTITY, 0);
            Stock stockAfterRemove = createStock(STOCK_QUANTITY - AMOUNT, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stockAfterRemove);

            Stock result = stockService.removeStock(PRODUCT_ID, AMOUNT);

            assertThat(result.getQuantity()).isEqualTo(STOCK_QUANTITY - AMOUNT);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should throw exception when amount is invalid")
        void shouldThrowExceptionWhenAmountIsInvalid(Integer invalidAmount) {
            Stock stock = createStock();

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.removeStock(PRODUCT_ID, invalidAmount))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(stockRepository, never()).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            Stock stock = createStock();

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.removeStock(PRODUCT_ID, null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(stockRepository, never()).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            Stock stock = createStock(3, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.removeStock(PRODUCT_ID, AMOUNT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient stock");

            verify(stockRepository, never()).save(any(Stock.class));
        }
    }

    // ============================================
    // RESERVE STOCK TESTS
    // ============================================

    @Nested
    @DisplayName("Reserve Stock Tests")
    class ReserveStockTests {

        @Test
        @DisplayName("Should reserve stock successfully")
        void shouldReserveStockSuccessfully() {
            Stock stock = createStock(STOCK_QUANTITY, 0);
            Stock stockAfterReserve = createStock(STOCK_QUANTITY, AMOUNT);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stockAfterReserve);

            Stock result = stockService.reserveStock(PRODUCT_ID, AMOUNT);

            assertThat(result.getReserved()).isEqualTo(AMOUNT);
            assertThat(result.getAvailable()).isEqualTo(STOCK_QUANTITY - AMOUNT);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should throw exception when amount is invalid")
        void shouldThrowExceptionWhenAmountIsInvalid(Integer invalidAmount) {
            Stock stock = createStock();

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.reserveStock(PRODUCT_ID, invalidAmount))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(stockRepository, never()).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            Stock stock = createStock();

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.reserveStock(PRODUCT_ID, null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(stockRepository, never()).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            Stock stock = createStock(3, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.reserveStock(PRODUCT_ID, AMOUNT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient stock");

            verify(stockRepository, never()).save(any(Stock.class));
        }
    }

    // ============================================
    // CONFIRM RESERVATION TESTS
    // ============================================

    @Nested
    @DisplayName("Confirm Reservation Tests")
    class ConfirmReservationTests {

        @Test
        @DisplayName("Should confirm reservation successfully")
        void shouldConfirmReservationSuccessfully() {
            Stock stock = createStock(STOCK_QUANTITY, AMOUNT);
            Stock stockAfterConfirm = createStock(STOCK_QUANTITY - AMOUNT, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stockAfterConfirm);

            Stock result = stockService.confirmReservation(PRODUCT_ID);

            assertThat(result.getReserved()).isZero();
            assertThat(result.getQuantity()).isEqualTo(STOCK_QUANTITY - AMOUNT);
        }

        @Test
        @DisplayName("Should throw exception when no reservation exists")
        void shouldThrowExceptionWhenNoReservation() {
            Stock stock = createStock(STOCK_QUANTITY, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.confirmReservation(PRODUCT_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No reservation to confirm");

            verify(stockRepository, never()).save(any(Stock.class));
        }
    }

    // ============================================
    // CANCEL RESERVATION TESTS
    // ============================================

    @Nested
    @DisplayName("Cancel Reservation Tests")
    class CancelReservationTests {

        @Test
        @DisplayName("Should cancel reservation successfully")
        void shouldCancelReservationSuccessfully() {
            Stock stock = createStock(STOCK_QUANTITY, AMOUNT);
            Stock stockAfterCancel = createStock(STOCK_QUANTITY, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenReturn(stockAfterCancel);

            Stock result = stockService.cancelReservation(PRODUCT_ID);

            assertThat(result.getReserved()).isZero();
            assertThat(result.getQuantity()).isEqualTo(STOCK_QUANTITY);
        }

        @Test
        @DisplayName("Should throw exception when no reservation exists")
        void shouldThrowExceptionWhenNoReservation() {
            Stock stock = createStock(STOCK_QUANTITY, 0);

            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> stockService.cancelReservation(PRODUCT_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No reservation to cancel");

            verify(stockRepository, never()).save(any(Stock.class));
        }
    }

    // ============================================
    // STOCK NOT FOUND TESTS (shared across methods)
    // ============================================

    @Nested
    @DisplayName("Stock Not Found Tests")
    class StockNotFoundTests {

        @Test
        @DisplayName("Should throw exception when stock not found on addStock")
        void shouldThrowOnAddStock() {
            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.addStock(PRODUCT_ID, AMOUNT))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when stock not found on removeStock")
        void shouldThrowOnRemoveStock() {
            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.removeStock(PRODUCT_ID, AMOUNT))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when stock not found on reserveStock")
        void shouldThrowOnReserveStock() {
            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.reserveStock(PRODUCT_ID, AMOUNT))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when stock not found on confirmReservation")
        void shouldThrowOnConfirmReservation() {
            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.confirmReservation(PRODUCT_ID))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when stock not found on cancelReservation")
        void shouldThrowOnCancelReservation() {
            when(stockRepository.findByProductId(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.cancelReservation(PRODUCT_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
