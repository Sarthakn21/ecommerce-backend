package com.sarthak.ecommerce.user.dto;

import com.sarthak.ecommerce.user.model.Role;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String name,
        String email,
        Role role,
        String storeName,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}