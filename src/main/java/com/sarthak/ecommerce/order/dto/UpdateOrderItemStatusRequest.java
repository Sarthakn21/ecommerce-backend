package com.sarthak.ecommerce.order.dto;

import com.sarthak.ecommerce.order.model.OrderItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderItemStatusRequest(
        @NotNull OrderItemStatus status,
        String reason
) {}
