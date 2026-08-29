package com.sarthak.ecommerce.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stock;
    private String imageUrl;

}
