package br.com.matheus.commerceapi.security.service;

import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.repository.StoreRepository;
import br.com.matheus.commerceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("storeSecurityService")
@RequiredArgsConstructor
public class StoreSecurityService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public boolean isStoreOwner(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId).orElse(null);
        return store != null && store.getStoreOwner().getId().equals(userId);
    }

    public boolean canCreateStore(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getUserRole() == UserRole.STOREOWNER;
    }
}