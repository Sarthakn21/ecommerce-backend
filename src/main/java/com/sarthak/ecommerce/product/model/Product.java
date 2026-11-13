package com.sarthak.ecommerce.product.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    @Indexed
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    @Indexed
    private String category;

    private Integer stock;

    private String imageUrl;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal averageRating = BigDecimal.ZERO;

    private Integer reviewCount = 0;

    private String sellerId;

    // Missing values in existing MongoDB documents retain this default.
    private boolean active = true;

    private LocalDateTime createdAt;
}
