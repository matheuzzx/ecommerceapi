package br.com.matheus.commerceapi.entity;

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
        checkValidAmount(amount);

        this.quantity += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public void removeStock(Integer amount) {
        checkAmount(amount);

        this.quantity -= amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public void reserve(Integer amount) {
        checkAmount(amount);

        this.reserved += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public void confirmReservation() {
        this.quantity -= this.reserved;
        this.reserved = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public void cancelReservation() {
        this.reserved = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    private void checkAmount(Integer amount){
        checkValidAmount(amount);
        if (amount > getAvailable()) throw new IllegalArgumentException("Insufficient stock. Available: " + getAvailable());
    }

    private void checkValidAmount(Integer amount){
        if(amount == null || amount <= 0) throw new IllegalArgumentException("Amount must be positive and not null");
    }

}
