package com.sarthak.ecommerce.product.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private BigDecimal price;

    @Indexed
    private String category;

    private Integer stock;

    private String imageUrl;

    private BigDecimal averageRating = BigDecimal.ZERO;

    private Integer reviewCount = 0;

    private LocalDateTime createdAt;
}