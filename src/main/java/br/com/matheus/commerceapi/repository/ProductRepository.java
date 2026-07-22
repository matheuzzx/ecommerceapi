package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameAndStoreId(String name, Long storeId);

    Optional<Product> findById(Long productId);

    List<Product> findByStore(Store store);

    Page<Product> findByStore(Store store, Pageable pageable);
}