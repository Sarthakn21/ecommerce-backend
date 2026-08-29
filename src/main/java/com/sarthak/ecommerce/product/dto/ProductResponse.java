package com.sarthak.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stock;
    private String imageUrl;
    private LocalDateTime createdAt;
}
