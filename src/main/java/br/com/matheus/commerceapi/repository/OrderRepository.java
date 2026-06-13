package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
