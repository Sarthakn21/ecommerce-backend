package com.sarthak.ecommerce.user.service;

import com.sarthak.ecommerce.common.exception.ApiException;
import com.sarthak.ecommerce.user.dto.UpdateUserRequest;
import com.sarthak.ecommerce.user.dto.UserRequest;
import com.sarthak.ecommerce.user.dto.UserResponse;
import com.sarthak.ecommerce.user.exception.EmailAlreadyExistsException;
import com.sarthak.ecommerce.user.exception.InvalidUserRoleException;
import com.sarthak.ecommerce.user.exception.UserNotFoundException;
import com.sarthak.ecommerce.user.model.Role;
import com.sarthak.ecommerce.user.model.User;
import com.sarthak.ecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponse createUser(UserRequest request){
        if (request.role() != Role.USER && request.role() != Role.SELLER) {
            throw new InvalidUserRoleException("Invalid user role");
        }
        if(userRepository.existsByEmail(request.email())){
            throw new EmailAlreadyExistsException("User with email already exists: " + request.email());
        }

        if (request.role() == Role.SELLER && (request.storeName() == null || request.storeName().isBlank())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Missing store name");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
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

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public UserResponse getUser(String id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        return mapToResponse(user);
    }
    public List<UserResponse> getUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse updateUser(String id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        user.setName(request.name());
        user.setEmail(request.email());
        if (user.getRole() == Role.SELLER) {

            if (request.storeName() == null || request.storeName().isBlank()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Store name is required for seller"
                );
            }

            user.setStoreName(request.storeName());

        } else {
            user.setStoreName(null);
        }
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(String id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        userRepository.delete(user);
    }
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStoreName(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
