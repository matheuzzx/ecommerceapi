package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameAndStoreId(String name, Long storeId);
    List<Product> findByStore(Store store);
}
