package br.com.matheus.commerceapi.security.service;

import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public boolean canCreateStore(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUserRole() == UserRole.STOREOWNER)
                .orElse(false);
    }
}