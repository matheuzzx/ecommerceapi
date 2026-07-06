package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    Page<Category> findAll(Pageable pageable);
    Page<Category> findByDisplayNameContaining(String name, Pageable pageable);
}
