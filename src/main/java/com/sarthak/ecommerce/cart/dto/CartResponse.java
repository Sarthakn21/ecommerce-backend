package com.sarthak.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        String cartId,
        String userId,
        List<CartItemResponse> items,
        BigDecimal total
) {
}