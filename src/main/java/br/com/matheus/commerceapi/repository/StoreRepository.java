package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsBySlug(String slug);
}
