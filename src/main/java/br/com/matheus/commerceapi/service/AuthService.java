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
    JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    public String login(String email, String rawPassword){

        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new RuntimeException("email and password are required");
        }

        String trimmedEmail = email.trim();

        var possibleUser = userRepository.findByEmail(trimmedEmail);

        if(possibleUser.isEmpty()) throw new RuntimeException("user not registered");

        var user = possibleUser.get();

        if(!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("invalid credentials");
        }

        return jwtService.generateToken(trimmedEmail, user.getUserRole().toString());
    }
}
