package com.sarthak.ecommerce.order.dto;

import com.sarthak.ecommerce.order.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SellerOrderResponse(
        String orderId,
        String customerId,
        OrderStatus status,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
