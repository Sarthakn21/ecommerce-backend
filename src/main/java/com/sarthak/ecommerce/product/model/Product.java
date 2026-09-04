package com.sarthak.ecommerce.product.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

    private String category;

    private Integer stock;

    private String imageUrl;

    private BigDecimal averageRating = BigDecimal.ZERO;

    private Integer reviewCount = 0;

    private LocalDateTime createdAt;
}