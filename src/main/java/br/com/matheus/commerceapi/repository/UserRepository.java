package br.com.matheus.commerceapi.repository;

import br.com.matheus.commerceapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}
