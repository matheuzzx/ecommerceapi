package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerId(Long userId, Pageable pageable);
    Page<Order> findByStoreId(Long storeId, Pageable pageable);
    Optional<Order> findByCustomerIdAndId(Long customerId, Long id);
    Optional<Order> findByStore_StoreOwnerIdAndId(Long storeOwnerId, Long id);
}
