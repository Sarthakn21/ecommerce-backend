package com.sarthak.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewResponse {

    private String id;
    private String productId;
    private String userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}