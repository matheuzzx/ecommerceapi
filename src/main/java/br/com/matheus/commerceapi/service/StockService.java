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
public class StockService {

    private final StockRepository stockRepository;

    public Stock createStockForProduct(Product product, Integer quantity) {
        Stock stock = Stock.builder()
                .product(product)
                .quantity(quantity != null ? quantity : 0)
                .reserved(0)
                .lastUpdated(LocalDateTime.now())
                .build();

        return stockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public Stock getStockByProduct(Product product) {
        return stockRepository.findByProduct(product)
                .orElseThrow(() -> new NotFoundException("Stock Not Found"));
    }

    @Transactional(readOnly = true)
    public Stock getStockByProductId(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock Not Found"));
    }

    @Transactional
    public Stock addStock(Long productId, Integer amount){
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock Not Found"));

        stock.addStock(amount);

        return stockRepository.save(stock);
    }

    @Transactional
    public Stock removeStock(Long productId, Integer amount) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock Not Found"));

        stock.removeStock(amount);

        return stockRepository.save(stock);
    }

    @Transactional
    public Stock reserveStock(Long productId, Integer amount) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Stock Not Found"));

        stock.reserve(amount);

        return stockRepository.save(stock);
    }
}
