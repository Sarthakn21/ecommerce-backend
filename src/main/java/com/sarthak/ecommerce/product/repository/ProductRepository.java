package com.sarthak.ecommerce.product.repository;

import com.sarthak.ecommerce.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    List<Product> findBySellerIdOrderByCreatedAtDesc(String sellerId);

    @Query("{ '$or': [ { 'active': true }, { 'active': { '$exists': false } } ] }")
    Page<Product> findAvailableProducts(Pageable pageable);

    @Query("{ 'name': { '$regex': ?0, '$options': 'i' }, '$or': [ { 'active': true }, { 'active': { '$exists': false } } ] }")
    Page<Product> searchAvailableProducts(String name, Pageable pageable);

    @Query("{ 'price': { '$gte': ?0, '$lte': ?1 }, '$or': [ { 'active': true }, { 'active': { '$exists': false } } ] }")
    Page<Product> findAvailableProductsByPrice(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );
}
