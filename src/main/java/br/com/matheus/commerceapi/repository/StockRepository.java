package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProduct(Product product);
}
