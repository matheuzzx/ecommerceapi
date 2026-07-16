package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsBySlug(String slug);
    Optional<Store> findById(Long id);
    boolean existsByStoreOwner(User user);
    Optional<Store> findByStoreOwnerId(Long userId);
}
