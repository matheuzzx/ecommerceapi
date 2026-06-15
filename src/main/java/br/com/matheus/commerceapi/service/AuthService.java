package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.entity.User;
import br.com.matheus.commerceapi.enums.UserRole;
import br.com.matheus.commerceapi.repository.UserRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String name, String email, String rawPassword, String userRole){

        if(name.isBlank() || email.isBlank() || rawPassword.isBlank()){
            throw new RuntimeException("required field is empty");
        }

        String trimmedName = name.trim();
        String trimmedEmail = email.trim().toLowerCase();

        var emailIsValid = EmailValidator.getInstance().isValid(trimmedEmail);

        if(!emailIsValid) throw new RuntimeException("email is not valid");

        var emailExists = userRepository.existsByEmail(trimmedEmail);

        if(emailExists) throw new RuntimeException("user already exists");

        UserRole role = UserRole.valueOf(userRole.toUpperCase());

        if(role != UserRole.CUSTOMER && role != UserRole.STOREOWNER) throw new RuntimeException("invalid role");

        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .name(trimmedName)
                .email(trimmedEmail)
                .passwordHash(hashedPassword)
                .userRole(role)
                .build();

        return userRepository.save(user);
    }
}
