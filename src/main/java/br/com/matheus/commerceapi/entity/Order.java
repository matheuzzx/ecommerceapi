package br.com.matheus.commerceapi.entity;

import br.com.matheus.commerceapi.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private CreationTimestamp createdAt;

    @Column(name = "updated_at")
    private UpdateTimestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.CREATED;
        }
    }

    public void nextStatus() {
        if (this.status == null) {
            this.status = OrderStatus.CREATED;
        }
        this.status = this.status.next();
    }

    public boolean canCancel() {
        return status == OrderStatus.CREATED || status == OrderStatus.PAID;
    }

    public void cancel() {
        if (!canCancel()) {
            throw new RuntimeException("Pedido não pode ser cancelado no status: " + status);
        }
        this.status = OrderStatus.CANCELED;
    }

}
