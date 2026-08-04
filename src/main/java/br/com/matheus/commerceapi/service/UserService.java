package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.user.UpdateUserRequestDto;
import br.com.matheus.commerceapi.dto.response.auth.UserResponseDto;
import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.exception.EmailAlreadyExistsException;
import br.com.matheus.commerceapi.exception.UserNotFoundException;
import br.com.matheus.commerceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto request) {
        User user = findUserById(userId);

        user.setName(request.name());

        User savedUser = userRepository.save(user);

        log.info("User updated: {} (ID: {})", savedUser.getName(), userId);

        return UserResponseDto.fromEntity(savedUser);
    }

    public void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            throw new EmailAlreadyExistsException(email);
        }
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
