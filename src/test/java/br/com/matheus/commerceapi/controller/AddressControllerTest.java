package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.address.CreateAddressRequestDto;
import br.com.matheus.commerceapi.dto.request.address.UpdateAddressRequestDto;
import br.com.matheus.commerceapi.dto.response.address.AddressResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import br.com.matheus.commerceapi.service.AddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressController Tests")
class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 1L;

    private UserDetailsImpl createUserDetails() {
        User user = User.builder().id(USER_ID).build();
        return new UserDetailsImpl(user);
    }

    @Test
    @DisplayName("Should create address and return 201")
    void shouldCreateAddressAndReturnCreated() {
        CreateAddressRequestDto request = new CreateAddressRequestDto("Rua A", "1", "São Paulo", "SP", "01000-000");
        when(addressService.createAddress(eq(USER_ID), any(CreateAddressRequestDto.class)))
                .thenReturn(null);

        ResponseEntity<AddressResponseDto> result = addressController.createAddress(createUserDetails(), request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Should list addresses and return 200")
    void shouldListAddressesAndReturnOk() {
        when(addressService.getAddresses(USER_ID)).thenReturn(List.of());

        ResponseEntity<List<AddressResponseDto>> result = addressController.getAddresses(createUserDetails());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should update address and return 200")
    void shouldUpdateAddressAndReturnOk() {
        UpdateAddressRequestDto request = new UpdateAddressRequestDto("Rua B", null, null, null, null);
        when(addressService.updateAddress(eq(USER_ID), eq(ADDRESS_ID), any(UpdateAddressRequestDto.class)))
                .thenReturn(null);

        ResponseEntity<AddressResponseDto> result = addressController.updateAddress(createUserDetails(), ADDRESS_ID, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should delete address and return 204")
    void shouldDeleteAddressAndReturnNoContent() {
        ResponseEntity<Void> result = addressController.deleteAddress(createUserDetails(), ADDRESS_ID);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(addressService).deleteAddress(USER_ID, ADDRESS_ID);
    }
}