package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.store.CreateStoreRequestDto;
import br.com.matheus.commerceapi.dto.request.store.UpdateStoreRequestDto;
import br.com.matheus.commerceapi.dto.response.store.StoreResponseDto;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final  StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    @PreAuthorize("@securityService.canCreateStore(#userDetails.id)")
    public ResponseEntity<StoreResponseDto> register(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreateStoreRequestDto request) {

        StoreResponseDto store = storeService.createStore(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("@securityService.isStoreOwner(#storeId, #userDetails.id) or hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDto> getStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long storeId) {

        StoreResponseDto response = storeService.getStore(storeId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{storeId}")
    @PreAuthorize("@securityService.isStoreOwner(#storeId, #userDetails.id)")
    public ResponseEntity<StoreResponseDto> updateStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long storeId,
            @RequestBody @Valid UpdateStoreRequestDto request) {

        StoreResponseDto response = storeService.updateStore(storeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{storeId}")
    @PreAuthorize("@securityService.isStoreOwner(#storeId, #userDetails.id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long storeId) {

        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }

}
