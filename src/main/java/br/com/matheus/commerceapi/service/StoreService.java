package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.CreateStoreRequestDto;
import br.com.matheus.commerceapi.dto.response.StoreResponseDto;
import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.exception.InvalidRoleException;
import br.com.matheus.commerceapi.exception.SlugAlreadyExistsException;
import br.com.matheus.commerceapi.repository.StoreRepository;
import br.com.matheus.commerceapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

import static br.com.matheus.commerceapi.utils.ValidationUtils.validateEmailFormat;
import static br.com.matheus.commerceapi.utils.ValidationUtils.validateRequired;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public StoreResponseDto createStore(CreateStoreRequestDto request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        if (user.getUserRole() != UserRole.STOREOWNER) {
            throw new InvalidRoleException("Invalid role, Role Accepted is STOREOWNER");
        }

        validateRequired(Map.of(
                "Name", request.name(),
                "Email", request.email()
        ));

        String trimmedEmail = request.email().trim();
        validateEmailFormat(trimmedEmail);

        String slug = request.name().replace(" ", "_");

        if (storeRepository.existsBySlug(slug)) {
            throw new SlugAlreadyExistsException(slug);
        }

        Store store = Store.builder()
                .storeOwner(user)
                .name(request.name())
                .email(trimmedEmail)
                .active(true)
                .slug(slug)
                .build();

        user.setStore(store);

        Store savedStore = storeRepository.save(store);

        return StoreResponseDto.fromEntity(savedStore);
    }
}