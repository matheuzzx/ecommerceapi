package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameAndStoreId(String name, Long storeId);
}
