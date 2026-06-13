package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {}
