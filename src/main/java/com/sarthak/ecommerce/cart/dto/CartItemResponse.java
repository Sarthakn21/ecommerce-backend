package com.sarthak.ecommerce.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        String productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {
}