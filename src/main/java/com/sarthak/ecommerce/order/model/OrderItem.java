package com.sarthak.ecommerce.order.model;

import com.sarthak.ecommerce.order.model.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String itemId;

    private String productId;

    private String sellerId;

    private String productName;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    private Integer quantity;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;

    private OrderItemStatus status;

    private String cancellationReason;
}
