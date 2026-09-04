package com.sarthak.ecommerce.product.repository;

import com.sarthak.ecommerce.product.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<Review,String> {
    Page<Review> findByProductId(String productId, Pageable pageable);

}
