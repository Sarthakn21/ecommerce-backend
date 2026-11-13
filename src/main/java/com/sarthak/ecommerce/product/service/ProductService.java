package com.sarthak.ecommerce.product.service;

import com.sarthak.ecommerce.common.dto.PageResponse;
import com.sarthak.ecommerce.product.dto.ProductRequest;
import com.sarthak.ecommerce.product.dto.ProductResponse;
import com.sarthak.ecommerce.product.exception.ProductNotFoundException;
import com.sarthak.ecommerce.product.model.Product;
import com.sarthak.ecommerce.product.repository.ProductRepository;
import com.sarthak.ecommerce.product.util.ProductUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductUtil productUtil;

    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        return toPageResponse(productRepository.findAvailableProducts(pageable));
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return mapToResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setAverageRating(BigDecimal.ZERO);
        product.setReviewCount(0);
        product.setActive(true);
        if (!productUtil.isAdmin()) {
            product.setSellerId(productUtil.getAuthenticatedUserId());
        }
        product.setCreatedAt(LocalDateTime.now());
        return mapToResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product oldProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productUtil.verifyProductOwnership(oldProduct);
        oldProduct.setName(request.getName());
        oldProduct.setDescription(request.getDescription());
        oldProduct.setPrice(request.getPrice());
        oldProduct.setImageUrl(request.getImageUrl());
        oldProduct.setStock(request.getStock());
        oldProduct.setCategory(request.getCategory());
        return mapToResponse(productRepository.save(oldProduct));
    }

    public String deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productUtil.verifyProductOwnership(product);
        productRepository.delete(product);
        return "Product with id " + id + " deleted successfully";
    }

    public List<ProductResponse> getMyProducts() {
        String sellerId = productUtil.getAuthenticatedUserId();
        return productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse deactivateProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productUtil.verifyProductOwnership(product);
        product.setActive(false);
        return mapToResponse(productRepository.save(product));
    }

    public PageResponse<ProductResponse> searchProducts(String name, Pageable pageable) {
        return toPageResponse(productRepository.searchAvailableProducts(
                java.util.regex.Pattern.quote(name), pageable));
    }

    public PageResponse<ProductResponse> getFilteredProducts(
            BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return toPageResponse(productRepository.findAvailableProductsByPrice(
                minPrice, maxPrice, pageable));
    }

    private PageResponse<ProductResponse> toPageResponse(Page<Product> productPage) {
        Page<ProductResponse> responsePage = productPage.map(this::mapToResponse);
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

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setCategory(product.getCategory());
        response.setStock(product.getStock());
        response.setImageUrl(product.getImageUrl());
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        response.setSellerId(product.getSellerId());
        response.setActive(product.isActive());
        response.setCreatedAt(product.getCreatedAt());
        return response;
    }
}
