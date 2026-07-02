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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService Tests")
public class StoreServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ValidationUtils validationUtils;

    @InjectMocks
    private StoreService storeService;

    private static final Long USER_ID = 1L;
    private static final Long STORE_ID = 1L;
    private static final String NAME = "Minha Loja";
    private static final String EMAIL = "loja@email.com";
    private static final String SLUG = "Minha_Loja";
    private static final String UPDATED_NAME = "Nova Loja";

    // ============================================
    // CREATE STORE TESTS
    // ============================================

    @Nested
    @DisplayName("Create Store Tests")
    class CreateStoreTests {

        // ===== SUCCESS SCENARIOS =====

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should create store successfully")
            void shouldCreateStoreSuccessfully() {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, EMAIL);
                User user = createValidStoreOwner();
                Store store = createStore(user);

                mockUserValidation(user);
                mockValidationPass();
                when(storeRepository.existsBySlug(SLUG)).thenReturn(false);
                when(storeRepository.save(any(Store.class))).thenReturn(store);

                // Act
                StoreResponseDto response = storeService.createStore(request, USER_ID);

                // Assert
                assertResponse(response);
                assertStoreSavedCorrectly(user);
            }

            @Test
            @DisplayName("Should set store as active by default")
            void shouldSetStoreAsActiveByDefault() {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, EMAIL);
                User user = createValidStoreOwner();

                mockUserValidation(user);
                mockValidationPass();
                when(storeRepository.existsBySlug(SLUG)).thenReturn(false);
                when(storeRepository.save(any(Store.class))).thenReturn(createStore(user));

                // Act
                storeService.createStore(request, USER_ID);

                // Assert
                ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
                verify(storeRepository).save(storeCaptor.capture());
                assertThat(storeCaptor.getValue().isActive()).isTrue();
            }

            @Test
            @DisplayName("Should associate store with user")
            void shouldAssociateStoreWithUser() {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, EMAIL);
                User user = createValidStoreOwner();
                Store savedStore = createStore(user);

                mockUserValidation(user);
                mockValidationPass();
                when(storeRepository.existsBySlug(SLUG)).thenReturn(false);
                when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

                // Act
                storeService.createStore(request, USER_ID);

                // Assert
                assertThat(user.getStore()).isNotNull();
                assertThat(user.getStore().getName()).isEqualTo(NAME);
                assertThat(user.getStore().getEmail()).isEqualTo(EMAIL);
                assertThat(user.getStore().getSlug()).isEqualTo(SLUG);
                assertThat(user.getStore().isActive()).isTrue();

                ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
                verify(storeRepository).save(storeCaptor.capture());
                assertThat(storeCaptor.getValue().getStoreOwner()).isEqualTo(user);
            }

            @ParameterizedTest
            @ValueSource(strings = {"Minha Loja", "Loja Dois", "Loja_Teste", "  Com Espacos  "})
            @DisplayName("Should convert name to slug correctly")
            void shouldConvertNameToSlugCorrectly(String name) {
                // Arrange
                String expectedSlug = name.replace(" ", "_");
                CreateStoreRequestDto request = new CreateStoreRequestDto(name, EMAIL);
                User user = createValidStoreOwner();

                mockUserValidation(user);
                mockValidationPass();
                when(storeRepository.existsBySlug(expectedSlug)).thenReturn(false);
                when(storeRepository.save(any(Store.class))).thenReturn(createStore(user));

                // Act
                storeService.createStore(request, USER_ID);

                // Assert
                ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
                verify(storeRepository).save(storeCaptor.capture());
                assertThat(storeCaptor.getValue().getSlug()).isEqualTo(expectedSlug);
            }

            @Test
            @DisplayName("Should trim email before validation")
            void shouldTrimEmailBeforeValidation() {
                // Arrange
                String emailWithSpaces = "  loja@email.com  ";
                String trimmedEmail = "loja@email.com";
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, emailWithSpaces);
                User user = createValidStoreOwner();

                mockUserValidation(user);
                doNothing().when(validationUtils).validateRequired(any());
                doNothing().when(validationUtils).validateEmailFormat(trimmedEmail);
                when(storeRepository.existsBySlug(SLUG)).thenReturn(false);
                when(storeRepository.save(any(Store.class))).thenReturn(createStore(user));

                // Act
                storeService.createStore(request, USER_ID);

                // Assert
                verify(validationUtils).validateEmailFormat(trimmedEmail);
                ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
                verify(storeRepository).save(storeCaptor.capture());
                assertThat(storeCaptor.getValue().getEmail()).isEqualTo(trimmedEmail);
            }
        }

        // ===== VALIDATION TESTS =====

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "  ", "\t"})
            @DisplayName("Should throw exception when name is invalid")
            void shouldThrowExceptionWhenNameIsInvalid(String invalidName) {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(invalidName, EMAIL);

                doThrow(new IllegalArgumentException("Name cannot be null or blank"))
                        .when(validationUtils).validateRequired(any());

                // Act & Assert
                assertThrows(IllegalArgumentException.class,
                        () -> storeService.createStore(request, USER_ID));

                verifyNoInteractionsWithRepository();
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "  ", "\t"})
            @DisplayName("Should throw exception when email is invalid")
            void shouldThrowExceptionWhenEmailIsInvalid(String invalidEmail) {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, invalidEmail);

                doThrow(new IllegalArgumentException("Email cannot be null or blank"))
                        .when(validationUtils).validateRequired(any());

                // Act & Assert
                assertThrows(IllegalArgumentException.class,
                        () -> storeService.createStore(request, USER_ID));

                verifyNoInteractionsWithRepository();
            }

            @Test
            @DisplayName("Should throw exception when email format is invalid")
            void shouldThrowExceptionWhenEmailFormatIsInvalid() {
                // Arrange
                String invalidEmail = "invalid_email";
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, invalidEmail);
                User user = createValidStoreOwner();

                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
                when(storeRepository.existsByStoreOwner(user)).thenReturn(false);
                doNothing().when(validationUtils).validateRequired(any());
                doThrow(new IllegalArgumentException("Email is not valid"))
                        .when(validationUtils).validateEmailFormat(invalidEmail.trim());

                // Act & Assert
                assertThatThrownBy(() -> storeService.createStore(request, USER_ID))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Email is not valid");

                verify(storeRepository, never()).save(any(Store.class));
            }
        }

        // ===== EXCEPTION TESTS =====

        @Nested
        @DisplayName("Exception Tests")
        class ExceptionTests {

            @Test
            @DisplayName("Should throw exception when user not found")
            void shouldThrowExceptionWhenUserNotFound() {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, EMAIL);

                when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> storeService.createStore(request, USER_ID))
                        .isInstanceOf(UsernameNotFoundException.class)
                        .hasMessage("User Not Found");

                verify(storeRepository, never()).save(any(Store.class));
                verify(storeRepository, never()).existsByStoreOwner(any());
            }

            @Test
            @DisplayName("Should throw exception when user is not STOREOWNER")
            void shouldThrowExceptionWhenUserIsNotStoreOwner() {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, EMAIL);
                User user = createUserWithRole(UserRole.CUSTOMER);

                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

                // Act & Assert
                assertThatThrownBy(() -> storeService.createStore(request, USER_ID))
                        .isInstanceOf(InvalidRoleException.class)
                        .hasMessage("Invalid role, Role Accepted is STOREOWNER");

                verify(storeRepository, never()).save(any(Store.class));
                verify(storeRepository, never()).existsByStoreOwner(any());
            }

            @Test
            @DisplayName("Should throw exception when user already has a store")
            void shouldThrowExceptionWhenUserAlreadyHasStore() {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, EMAIL);
                User user = createValidStoreOwner();

                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
                when(storeRepository.existsByStoreOwner(user)).thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> storeService.createStore(request, USER_ID))
                        .isInstanceOf(StoreAlreadyExists.class);

                verify(storeRepository, never()).save(any(Store.class));
            }

            @Test
            @DisplayName("Should throw exception when slug already exists")
            void shouldThrowExceptionWhenSlugAlreadyExists() {
                // Arrange
                CreateStoreRequestDto request = new CreateStoreRequestDto(NAME, EMAIL);
                User user = createValidStoreOwner();

                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
                when(storeRepository.existsByStoreOwner(user)).thenReturn(false);
                doNothing().when(validationUtils).validateRequired(any());
                doNothing().when(validationUtils).validateEmailFormat(EMAIL.trim());
                when(storeRepository.existsBySlug(SLUG)).thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> storeService.createStore(request, USER_ID))
                        .isInstanceOf(SlugAlreadyExistsException.class);

                verify(storeRepository, never()).save(any(Store.class));
            }
        }
    }

    // ============================================
    // GET STORE TESTS
    // ============================================

    @Nested
    @DisplayName("Get Store Tests")
    class GetStoreTests {

        @Test
        @DisplayName("Should get store successfully when it exists")
        void shouldGetStoreSuccessfully() {
            // Arrange
            User user = createValidStoreOwner();
            Store store = createStore(user);

            when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

            // Act
            StoreResponseDto response = storeService.getStore(STORE_ID);

            // Assert
            assertThat(response.id()).isEqualTo(STORE_ID);
            assertThat(response.name()).isEqualTo(NAME);
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.slug()).isEqualTo(SLUG);
            assertThat(response.active()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            // Arrange
            when(storeRepository.findById(STORE_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> storeService.getStore(STORE_ID))
                    .isInstanceOf(StoreNotFoundException.class);
        }
    }

    // ============================================
    // UPDATE STORE TESTS
    // ============================================

    @Nested
    @DisplayName("Update Store Tests")
    class UpdateStoreTests {

        @Test
        @DisplayName("Should update store name successfully")
        void shouldUpdateStoreSuccessfully() {
            // Arrange
            UpdateStoreRequestDto request = new UpdateStoreRequestDto(UPDATED_NAME);
            User user = createValidStoreOwner();
            Store store = createStore(user);
            Store updatedStore = createUpdatedStore(user);

            when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
            doNothing().when(validationUtils).validateRequired(any());
            when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

            // Act
            StoreResponseDto response = storeService.updateStore(STORE_ID, request);

            // Assert
            assertThat(response.id()).isEqualTo(STORE_ID);
            assertThat(response.name()).isEqualTo(UPDATED_NAME);
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.slug()).isEqualTo(SLUG);
            assertThat(response.active()).isTrue();

            ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
            verify(storeRepository).save(storeCaptor.capture());
            assertThat(storeCaptor.getValue().getName()).isEqualTo(UPDATED_NAME);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t"})
        @DisplayName("Should throw exception when name is invalid")
        void shouldThrowExceptionWhenNameIsInvalid(String invalidName) {
            // Arrange
            UpdateStoreRequestDto request = new UpdateStoreRequestDto(invalidName);

            doThrow(new IllegalArgumentException("Name cannot be null or blank"))
                    .when(validationUtils).validateRequired(any());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> storeService.updateStore(STORE_ID, request));

            verify(storeRepository, never()).findById(any());
            verify(storeRepository, never()).save(any(Store.class));
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            // Arrange
            UpdateStoreRequestDto request = new UpdateStoreRequestDto(UPDATED_NAME);

            when(storeRepository.findById(STORE_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> storeService.updateStore(STORE_ID, request))
                    .isInstanceOf(StoreNotFoundException.class);

            verify(storeRepository, never()).save(any(Store.class));
        }
    }

    // ============================================
    // DELETE STORE TESTS
    // ============================================

    @Nested
    @DisplayName("Delete Store Tests")
    class DeleteStoreTests {

        @Test
        @DisplayName("Should delete store successfully")
        void shouldDeleteStoreSuccessfully() {
            // Arrange
            User user = createValidStoreOwner();
            Store store = createStore(user);

            when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

            // Act
            storeService.deleteStore(STORE_ID);

            // Assert
            verify(storeRepository).delete(store);
        }

        @Test
        @DisplayName("Should remove store association from user")
        void shouldRemoveStoreAssociationFromUser() {
            // Arrange
            User user = createValidStoreOwner();
            Store store = createStore(user);

            when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));

            // Act
            storeService.deleteStore(STORE_ID);

            // Assert
            assertThat(user.getStore()).isNull();
            verify(storeRepository).delete(store);
        }

        @Test
        @DisplayName("Should throw exception when store not found")
        void shouldThrowExceptionWhenStoreNotFound() {
            // Arrange
            when(storeRepository.findById(STORE_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> storeService.deleteStore(STORE_ID))
                    .isInstanceOf(StoreNotFoundException.class);

            verify(storeRepository, never()).delete(any(Store.class));
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private User createValidStoreOwner() {
        return User.builder()
                .id(USER_ID)
                .name("User Name")
                .email("user@email.com")
                .passwordHash("hashed")
                .userRole(UserRole.STOREOWNER)
                .store(null)
                .build();
    }

    private User createUserWithRole(UserRole role) {
        return User.builder()
                .id(USER_ID)
                .name("User Name")
                .email("user@email.com")
                .passwordHash("hashed")
                .userRole(role)
                .store(null)
                .build();
    }

    private Store createStore(User user) {
        return Store.builder()
                .id(STORE_ID)
                .storeOwner(user)
                .name(NAME)
                .email(EMAIL)
                .active(true)
                .slug(SLUG)
                .build();
    }

    private Store createUpdatedStore(User user) {
        return Store.builder()
                .id(STORE_ID)
                .storeOwner(user)
                .name(UPDATED_NAME)
                .email(EMAIL)
                .active(true)
                .slug(SLUG)
                .build();
    }

    private void mockUserValidation(User user) {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storeRepository.existsByStoreOwner(user)).thenReturn(false);
    }

    private void mockValidationPass() {
        doNothing().when(validationUtils).validateRequired(any());
        doNothing().when(validationUtils).validateEmailFormat(any());
    }

    private void assertResponse(StoreResponseDto response) {
        assertThat(response.id()).isEqualTo(STORE_ID);
        assertThat(response.name()).isEqualTo(NAME);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.slug()).isEqualTo(SLUG);
        assertThat(response.active()).isTrue();
    }

    private void assertStoreSavedCorrectly(User user) {
        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
        verify(storeRepository).save(storeCaptor.capture());
        Store capturedStore = storeCaptor.getValue();
        assertThat(capturedStore.getStoreOwner()).isEqualTo(user);
        assertThat(capturedStore.getName()).isEqualTo(NAME);
        assertThat(capturedStore.getEmail()).isEqualTo(EMAIL);
        assertThat(capturedStore.getSlug()).isEqualTo(SLUG);
        assertThat(capturedStore.isActive()).isTrue();
    }

    private void verifyNoInteractionsWithRepository() {
        verify(userRepository, never()).findById(any());
        verify(storeRepository, never()).save(any(Store.class));
        verify(storeRepository, never()).existsByStoreOwner(any());
    }
}