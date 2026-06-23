package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.CreateStoreRequestDto;
import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.exception.BusinessException;
import br.com.matheus.commerceapi.repository.StoreRepository;
import br.com.matheus.commerceapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    public Store createStore(CreateStoreRequestDto request, Long userId){

        validateRequired(Map.of(
                "Name", request.name(),
                "Email", request.email()
        ));

        String trimmedEmail = request.email().trim();
        validateEmailFormat(trimmedEmail);

        String slug = request.name().replace(" ", "_");

        boolean storeExists = storeRepository.existsBySlug(slug);

        if(storeExists) throw new BusinessException("this name is already registered", HttpStatus.BAD_REQUEST);

        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        Store store = Store.builder()
                .storeOwner(user)
                .email(trimmedEmail)
                .slug(slug)
                .build();

        return storeRepository.save(store);
    }

}