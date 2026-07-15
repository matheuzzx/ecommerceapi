package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import br.com.matheus.commerceapi.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
