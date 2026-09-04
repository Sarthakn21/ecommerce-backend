package com.sarthak.ecommerce.product.controller;

import com.sarthak.ecommerce.common.dto.PageResponse;
import com.sarthak.ecommerce.product.dto.ReviewRequest;
import com.sarthak.ecommerce.product.dto.ReviewResponse;
import com.sarthak.ecommerce.product.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse addReview(
            @Valid @RequestBody ReviewRequest request) {

        return reviewService.addReview(request);
    }

    @GetMapping("/product/{productId}")
    public PageResponse<ReviewResponse> getReviewsByProduct(
            @PathVariable String productId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return reviewService.getReviewsByProduct(productId, pageable);
    }
}