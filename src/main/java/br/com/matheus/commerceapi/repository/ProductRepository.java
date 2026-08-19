package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Product;
import br.com.matheus.commerceapi.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameAndStoreId(String name, Long storeId);

    Optional<Product> findById(Long productId);

    Optional<Product> findByIdAndStore_StoreOwnerId(Long productId, Long storeOwnerId);

    List<Product> findByStore(Store store);

    Page<Product> findByStore(Store store, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true " +
           "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:storeId IS NULL OR p.store.id = :storeId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchActiveProducts(@Param("name") String name,
                                       @Param("categoryId") Long categoryId,
                                       @Param("storeId") Long storeId,
                                       @Param("minPrice") BigDecimal minPrice,
                                       @Param("maxPrice") BigDecimal maxPrice,
                                       Pageable pageable);
}