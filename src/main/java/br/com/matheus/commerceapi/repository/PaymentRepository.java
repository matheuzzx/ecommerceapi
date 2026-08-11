package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdAndOrder_CustomerId(Long id, Long customerId);
    Optional<Payment> findByTransactionId(String transactionId);
}