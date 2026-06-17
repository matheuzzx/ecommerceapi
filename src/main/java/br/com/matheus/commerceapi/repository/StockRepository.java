package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {}
