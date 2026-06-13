package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
