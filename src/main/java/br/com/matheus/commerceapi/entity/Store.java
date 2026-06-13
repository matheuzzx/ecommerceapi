package br.com.matheus.commerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "stores")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true ,nullable = false, length = 100)
    private String slug;

    @Column(unique = true ,nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @OneToOne
    @JoinColumn(name = "store_owner_id")
    private User storeOwner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public void activate(){
        this.active = true;
    }

    public void deactivate(){
        this.active = false;
    }
}
