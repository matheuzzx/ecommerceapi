package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.address.CreateAddressRequestDto;
import br.com.matheus.commerceapi.dto.request.address.UpdateAddressRequestDto;
import br.com.matheus.commerceapi.dto.response.address.AddressResponseDto;
import br.com.matheus.commerceapi.entity.Address;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Transactional
    public AddressResponseDto createAddress(Long userId, CreateAddressRequestDto request) {
        User user = userService.findUserById(userId);

        Address address = Address.builder()
                .street(request.street().trim())
                .number(request.number() != null ? request.number().trim() : null)
                .city(request.city().trim())
                .state(request.state().trim())
                .zipCode(request.zipCode().trim())
                .user(user)
                .build();

        Address savedAddress = addressRepository.save(address);

        log.info("Address created: ID {} for user {}", savedAddress.getId(), userId);

        return AddressResponseDto.fromEntity(savedAddress);
    }

    public List<AddressResponseDto> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(AddressResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public AddressResponseDto updateAddress(Long userId, Long addressId, UpdateAddressRequestDto request) {
        Address address = findAddressByIdAndUser(addressId, userId);

        if (request.street() != null && !request.street().isEmpty()) {
            address.setStreet(request.street().trim());
        }
        if (request.number() != null && !request.number().isEmpty()) {
            address.setNumber(request.number().trim());
        }
        if (request.city() != null && !request.city().isEmpty()) {
            address.setCity(request.city().trim());
        }
        if (request.state() != null && !request.state().isEmpty()) {
            address.setState(request.state().trim());
        }
        if (request.zipCode() != null && !request.zipCode().isEmpty()) {
            address.setZipCode(request.zipCode().trim());
        }

        Address updatedAddress = addressRepository.save(address);

        log.info("Address updated: ID {}", addressId);

        return AddressResponseDto.fromEntity(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = findAddressByIdAndUser(addressId, userId);
        addressRepository.delete(address);

        log.info("Address deleted: ID {}", addressId);
    }

    public Address findAddressByIdAndUser(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + addressId));
    }
}