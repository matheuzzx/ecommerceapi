package br.com.matheus.commerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(name = "created_at")
    private CreationTimestamp createdAt;

    @Column(name = "updated_at")
    private UpdateTimestamp updatedAt;

    public Integer getAvailable() {
        return quantity - reserved;
    }

    public void addStock(Integer amount) {
        this.quantity += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public void removeStock(Integer amount) {
        if (amount > getAvailable()) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + getAvailable());
        }
        this.quantity -= amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public void reserve(Integer amount) {
        if (amount > getAvailable()) {
            throw new RuntimeException("Estoque insuficiente para reserva. Disponível: " + getAvailable());
        }
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

}
