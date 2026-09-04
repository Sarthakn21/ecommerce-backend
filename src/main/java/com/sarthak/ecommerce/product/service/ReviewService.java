package com.sarthak.ecommerce.product.service;

import com.sarthak.ecommerce.common.dto.PageResponse;
import com.sarthak.ecommerce.product.dto.ReviewRequest;
import com.sarthak.ecommerce.product.dto.ReviewResponse;
import com.sarthak.ecommerce.product.exception.ProductNotFoundException;
import com.sarthak.ecommerce.product.model.Product;
import com.sarthak.ecommerce.product.model.Review;
import com.sarthak.ecommerce.product.repository.ProductRepository;
import com.sarthak.ecommerce.product.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {
    @Autowired
    ProductRepository productRepository;

    private final ReviewRepository reviewRepository;

    public ReviewResponse addReview(ReviewRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + request.getProductId()
                        ));

        Review review = new Review();

        review.setProductId(request.getProductId());
        review.setUserId(request.getUserId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        int currentReviewCount = product.getReviewCount();

        BigDecimal currentAverage = product.getAverageRating();
        BigDecimal totalRating = currentAverage.multiply(BigDecimal.valueOf(currentReviewCount));
        int newReviewCount = currentReviewCount + 1;
        BigDecimal newAverage =
                totalRating
                        .add(BigDecimal.valueOf(request.getRating()))
                        .divide(
                                BigDecimal.valueOf(newReviewCount),
                                2,
                                RoundingMode.HALF_UP
                        );

        product.setAverageRating(newAverage);
        product.setReviewCount(newReviewCount);

        productRepository.save(product);

        return mapToResponse(savedReview);
    }
    public PageResponse<ReviewResponse> getReviewsByProduct(String productId, Pageable pageable) {

        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);
        Page<ReviewResponse> responsePage = reviewPage.map(this::mapToResponse);

        return new PageResponse<>(
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages(),
                responsePage.isFirst(),
                responsePage.isLast(),
                responsePage.getContent()
        );
    }

    private ReviewResponse mapToResponse(Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}