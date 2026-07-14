package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);
    boolean existsByName(String name);
    Page<Category> findAll(Pageable pageable);
    boolean existsByNameAndIdNot(String name, Long CategoryId);
}
