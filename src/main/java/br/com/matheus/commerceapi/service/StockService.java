package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final StockRepository stockRepository;

    public Stock createStockForProduct(Product product) {
        Stock stock = Stock.builder()
                .product(product)
                .quantity(0)
                .reserved(0)
                .lastUpdated(LocalDateTime.now())
                .build();

        return stockRepository.save(stock);
    }

    public Stock getStockByProductId(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock Not Found"));
    }

    public Stock addStock(Long productId, Integer amount){
        Stock stock = getStockByProductId(productId);

        stock.addStock(amount);

        return stockRepository.save(stock);
    }

    public Stock removeStock(Long productId, Integer amount) {
        Stock stock = getStockByProductId(productId);

        stock.removeStock(amount);

        return stockRepository.save(stock);
    }

    public Stock reserveStock(Long productId, Integer amount) {
        Stock stock = getStockByProductId(productId);

        stock.reserve(amount);

        return stockRepository.save(stock);
    }

    public Stock confirmReservation(Long productId) {
        Stock stock = getStockByProductId(productId);

        stock.confirmReservation();

        return stockRepository.save(stock);
    }

    public Stock cancelReservation(Long productId) {
        Stock stock = getStockByProductId(productId);

        stock.cancelReservation();

        return stockRepository.save(stock);
    }
}
