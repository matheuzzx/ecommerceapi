package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.CreateStoreRequestDto;
import br.com.matheus.commerceapi.dto.response.StoreResponseDto;
import br.com.matheus.commerceapi.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class StoreController {

    StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<StoreResponseDto> register(@RequestBody CreateStoreRequestDto request, @PathVariable Long id){

        StoreResponseDto store = storeService.createStore(request, id);

        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

}
