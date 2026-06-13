package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}
