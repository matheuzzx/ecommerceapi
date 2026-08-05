package br.com.matheus.commerceapi.dto.response.address;

import br.com.matheus.commerceapi.entity.Address;

public record AddressResponseDto(
        Long id,
        String street,
        String number,
        String city,
        String state,
        String zipCode
) {
    public static AddressResponseDto fromEntity(Address address) {
        return new AddressResponseDto(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getCity(),
                address.getState(),
                address.getZipCode()
        );
    }
}