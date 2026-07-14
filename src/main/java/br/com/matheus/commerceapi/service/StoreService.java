package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.store.CreateStoreRequestDto;
import br.com.matheus.commerceapi.dto.request.store.UpdateStoreRequestDto;
import br.com.matheus.commerceapi.dto.response.store.StoreResponseDto;
import br.com.matheus.commerceapi.entity.Store;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.exception.InvalidRoleException;
import br.com.matheus.commerceapi.exception.SlugAlreadyExistsException;
import br.com.matheus.commerceapi.exception.StoreAlreadyExists;
import br.com.matheus.commerceapi.exception.StoreNotFoundException;
import br.com.matheus.commerceapi.repository.StoreRepository;
import br.com.matheus.commerceapi.repository.UserRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ValidationUtils validationUtils;

    public StoreResponseDto createStore(CreateStoreRequestDto request, Long userId) {

        Map<String, String> fields = new HashMap<>();
        fields.put("Name", request.name());
        fields.put("Email", request.email());

        validationUtils.validateRequiredString(fields);

        User user = validateAndGetUser(userId);

        validateExistingStore(user);

        String email = validateAndTrimEmail(request.email());

        String slug = toSlug(request.name());

        validateExistingSlug(slug);

        Store store = Store.builder()
                .storeOwner(user)
                .name(request.name())
                .email(email)
                .active(true)
                .slug(slug)
                .build();

        user.setStore(store);

        Store savedStore = storeRepository.save(store);

        return StoreResponseDto.fromEntity(savedStore);
    }

    public StoreResponseDto getStore(Long storeId){
        Store store = findStoreById(storeId);
        return StoreResponseDto.fromEntity(store);
    }

    public StoreResponseDto updateStore(Long storeId, UpdateStoreRequestDto request){

        Map<String, String> fields = new HashMap<>();
        fields.put("Name", request.name());

        validationUtils.validateRequiredString(fields);

        Store store = findStoreById(storeId);

        store.setName(request.name());

        Store savedStore = storeRepository.save(store);

        return StoreResponseDto.fromEntity(savedStore);
    }

    public void deleteStore(Long storeId){
        Store store = findStoreById(storeId);
        User storeOwner = store.getStoreOwner();
        storeOwner.setStore(null);
        storeRepository.delete(store);
    }

    private void validateExistingStore(User user){
        if(storeRepository.existsByStoreOwner(user)) throw new StoreAlreadyExists();
    }

    private String validateAndTrimEmail(String email){
        String trimmedEmail = email.trim();
        validationUtils.validateEmailFormat(trimmedEmail);
        return trimmedEmail;
    }

    private void validateExistingSlug(String slug){
        if (storeRepository.existsBySlug(slug)) {
            throw new SlugAlreadyExistsException(slug);
        }
    }

    private String toSlug(String name){
        return name.replace(" ", "_");
    }

    private User validateAndGetUser(Long userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        if (user.getUserRole() != UserRole.STOREOWNER) {
            throw new InvalidRoleException("Invalid role, Role Accepted is STOREOWNER");
        }

        return user;
    }

    private Store findStoreById(Long id){
        return storeRepository.findById(id).orElseThrow(StoreNotFoundException::new);
    }
}