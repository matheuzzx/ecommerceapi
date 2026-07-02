package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.repository.StoreRepository;
import br.com.matheus.commerceapi.repository.UserRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}
