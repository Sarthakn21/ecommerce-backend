package com.sarthak.ecommerce.auth.service;

import com.sarthak.ecommerce.auth.dto.LoginRequest;
import com.sarthak.ecommerce.auth.dto.LoginResponse;
import com.sarthak.ecommerce.auth.dto.RegisterRequest;
import com.sarthak.ecommerce.auth.security.JwtService;
import com.sarthak.ecommerce.common.exception.ApiException;
import com.sarthak.ecommerce.user.exception.EmailAlreadyExistsException;
import com.sarthak.ecommerce.user.model.Role;
import com.sarthak.ecommerce.user.model.User;
import com.sarthak.ecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "User with email already exists: " + request.email()
            );
        }

        if (request.role() == Role.Admin) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Admin registration is not allowed"
            );
        }

        if (request.role() == Role.SELLER) {

            if (request.storeName() == null ||
                    request.storeName().isBlank()) {

                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Store name is required for seller"
                );
            }
        }

        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setRole(request.role());

        if (request.role() == Role.SELLER) {
            user.setStoreName(request.storeName());
        } else {
            user.setStoreName(null);
        }

        user.setActive(true);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "User account is inactive"
            );
        }

        return new LoginResponse(
                jwtService.generateToken(user),
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }
}