package com.sarthak.ecommerce.order.dto;

import com.sarthak.ecommerce.order.model.OrderItemStatus;

import java.math.BigDecimal;

public record OrderItemResponse(
        String itemId,
        String productId,
        String sellerId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal,
        OrderItemStatus status,
        String cancellationReason
) {}
