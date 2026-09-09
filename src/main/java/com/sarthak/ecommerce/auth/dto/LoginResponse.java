package com.sarthak.ecommerce.auth.dto;

import com.sarthak.ecommerce.user.model.Role;

public record LoginResponse(
        String token,
        String userId,
        String name,
        Role role
) {
}