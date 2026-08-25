package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Payment;
import br.com.matheus.commerceapi.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdAndOrder_CustomerId(Long id, Long customerId);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
    List<Payment> findAllByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
