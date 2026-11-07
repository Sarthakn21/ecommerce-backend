package com.sarthak.ecommerce.order.dto;

import com.sarthak.ecommerce.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String orderId,
        String userId,
        List<OrderItemResponse> items,
        BigDecimal total,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
