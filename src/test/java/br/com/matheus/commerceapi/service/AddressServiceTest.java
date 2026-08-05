package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.address.CreateAddressRequestDto;
import br.com.matheus.commerceapi.dto.request.address.UpdateAddressRequestDto;
import br.com.matheus.commerceapi.dto.response.address.AddressResponseDto;
import br.com.matheus.commerceapi.entity.Address;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.exception.UserNotFoundException;
import br.com.matheus.commerceapi.repository.AddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Tests")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AddressService addressService;

    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 1L;
    private static final String STREET = "Rua das Flores, 123";
    private static final String CITY = "São Paulo";
    private static final String STATE = "SP";
    private static final String ZIP = "01000-000";

    private User createUser() {
        return User.builder().id(USER_ID).build();
    }

    private Address createAddress(User user) {
        return Address.builder()
                .id(ADDRESS_ID)
                .street(STREET)
                .city(CITY)
                .state(STATE)
                .zipCode(ZIP)
                .user(user)
                .build();
    }

    @Nested
    @DisplayName("Create Address Tests")
    class CreateAddressTests {

        @Test
        @DisplayName("Should create address successfully")
        void shouldCreateAddressSuccessfully() {
            CreateAddressRequestDto request = new CreateAddressRequestDto(STREET, "123", CITY, STATE, ZIP);
            when(userService.findUserById(USER_ID)).thenReturn(createUser());
            when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AddressResponseDto result = addressService.createAddress(USER_ID, request);

            assertThat(result.street()).isEqualTo(STREET);
            assertThat(result.city()).isEqualTo(CITY);

            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());
            assertThat(captor.getValue().getUser().getId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            CreateAddressRequestDto request = new CreateAddressRequestDto(STREET, "123", CITY, STATE, ZIP);
            when(userService.findUserById(USER_ID))
                    .thenThrow(new UserNotFoundException());

            assertThatThrownBy(() -> addressService.createAddress(USER_ID, request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Addresses Tests")
    class GetAddressesTests {

        @Test
        @DisplayName("Should return list of addresses")
        void shouldReturnListOfAddresses() {
            when(addressRepository.findByUserId(USER_ID))
                    .thenReturn(List.of(createAddress(createUser())));

            List<AddressResponseDto> result = addressService.getAddresses(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(ADDRESS_ID);
        }
    }

    @Nested
    @DisplayName("Update Address Tests")
    class UpdateAddressTests {

        @Test
        @DisplayName("Should update address successfully")
        void shouldUpdateAddressSuccessfully() {
            UpdateAddressRequestDto request = new UpdateAddressRequestDto("Nova Rua", null, null, null, null);
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(createAddress(createUser())));
            when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AddressResponseDto result = addressService.updateAddress(USER_ID, ADDRESS_ID, request);

            assertThat(result.street()).isEqualTo("Nova Rua");
            assertThat(result.city()).isEqualTo(CITY);
        }

        @Test
        @DisplayName("Should throw NotFound when address does not belong to user")
        void shouldThrowWhenAddressNotOwned() {
            UpdateAddressRequestDto request = new UpdateAddressRequestDto("Nova Rua", null, null, null, null);
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.updateAddress(USER_ID, ADDRESS_ID, request))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Address Tests")
    class DeleteAddressTests {

        @Test
        @DisplayName("Should delete address successfully")
        void shouldDeleteAddressSuccessfully() {
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(createAddress(createUser())));

            addressService.deleteAddress(USER_ID, ADDRESS_ID);

            verify(addressRepository).delete(any(Address.class));
        }

        @Test
        @DisplayName("Should throw NotFound when address does not belong to user")
        void shouldThrowWhenAddressNotOwned() {
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.deleteAddress(USER_ID, ADDRESS_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Find Address By Id And User Tests")
    class FindAddressByIdAndUserTests {

        @Test
        @DisplayName("Should return address when belongs to user")
        void shouldReturnAddressWhenOwned() {
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID))
                    .thenReturn(Optional.of(createAddress(createUser())));

            Address result = addressService.findAddressByIdAndUser(ADDRESS_ID, USER_ID);

            assertThat(result.getId()).isEqualTo(ADDRESS_ID);
        }

        @Test
        @DisplayName("Should throw NotFound when address does not belong to user")
        void shouldThrowWhenNotOwned() {
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.findAddressByIdAndUser(ADDRESS_ID, USER_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}