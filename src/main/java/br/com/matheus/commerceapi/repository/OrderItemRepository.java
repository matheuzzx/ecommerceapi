package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
