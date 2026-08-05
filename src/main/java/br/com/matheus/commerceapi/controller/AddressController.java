package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.address.CreateAddressRequestDto;
import br.com.matheus.commerceapi.dto.request.address.UpdateAddressRequestDto;
import br.com.matheus.commerceapi.dto.response.address.AddressResponseDto;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/me/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponseDto> createAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid CreateAddressRequestDto request) {

        AddressResponseDto address = addressService.createAddress(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getAddresses(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return ResponseEntity.ok(addressService.getAddresses(userDetails.getId()));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDto> updateAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long addressId,
            @RequestBody @Valid UpdateAddressRequestDto request) {

        AddressResponseDto address = addressService.updateAddress(userDetails.getId(), addressId, request);

        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long addressId) {

        addressService.deleteAddress(userDetails.getId(), addressId);

        return ResponseEntity.noContent().build();
    }
}