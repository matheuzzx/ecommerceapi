package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.CreateStoreRequestDto;
import br.com.matheus.commerceapi.dto.response.StoreResponseDto;
import br.com.matheus.commerceapi.security.UserDetailsImpl;
import br.com.matheus.commerceapi.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class StoreController {

    StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    @PreAuthorize("@storeSecurityService.canCreateStore(#authentication.principal.id)")
    public ResponseEntity<StoreResponseDto> register(@RequestBody CreateStoreRequestDto request,  Authentication authentication){
        Long userId = getCurrentUserId(authentication);
        StoreResponseDto store = storeService.createStore(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("@storeSecurityService.isStoreOwner(#storeId, #authentication.principal.id) or hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDto> getStore(
            @PathVariable Long storeId,
            Authentication authentication) {

        StoreResponseDto response = storeService.getStore(storeId);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        assert userDetails != null;
        return userDetails.getUser().getId();
    }

}
