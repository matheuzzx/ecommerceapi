package br.com.matheus.commerceapi.entity;

import br.com.matheus.commerceapi.exception.ConflictException;
import br.com.matheus.commerceapi.exception.InvalidArgumentException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer reserved = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Integer getAvailable() {
        return quantity - reserved;
    }

    public void addStock(Integer amount) {
        validatePositiveAmount(amount);
        this.quantity += amount;
        updateLastUpdated();
    }

    public void removeStock(Integer amount) {
        validateAmountWithStock(amount);
        this.quantity -= amount;
        updateLastUpdated();
    }

    public void reserve(Integer amount) {
        validateAmountWithStock(amount);
        this.reserved += amount;
        updateLastUpdated();
    }

    public void confirmReservation() {
        if (this.reserved == 0) {
            throw new ConflictException("No reservation to confirm for product: " + product.getId());
        }

        if (this.reserved > this.quantity) {
            throw new ConflictException("Reserved amount exceeds physical stock. Reserved: " +
                    this.reserved + ", Available: " + this.quantity);
        }

        this.quantity -= this.reserved;
        this.reserved = 0;
        updateLastUpdated();
    }

    public void cancelReservation() {
        if (this.reserved == 0) {
            throw new ConflictException("No reservation to cancel for product: " + product.getId());
        }

        this.reserved = 0;
        updateLastUpdated();
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidArgumentException("Amount must be positive and not null");
        }
    }

    private void validateAmountWithStock(Integer amount) {
        validatePositiveAmount(amount);
        if (amount > getAvailable()) {
            throw new InvalidArgumentException(
                    String.format("Insufficient stock. Requested: %d, Available: %d", amount, getAvailable())
            );
        }
    }

    private void updateLastUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }
}