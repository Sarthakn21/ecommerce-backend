package com.sarthak.ecommerce.product.service;

import com.sarthak.ecommerce.product.dto.ProductRequest;
import com.sarthak.ecommerce.product.dto.ProductResponse;
import com.sarthak.ecommerce.product.model.Product;
import com.sarthak.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse getProductById(String id){
        Product product= productRepository.findById(id).orElseThrow(()-> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request){
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCreatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    public ProductResponse updateProduct(String id, ProductRequest request){
        Product oldProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        oldProduct.setName(request.getName());
        oldProduct.setDescription(request.getDescription());
        oldProduct.setPrice(request.getPrice());
        oldProduct.setImageUrl(request.getImageUrl());
        oldProduct.setStock(request.getStock());
        oldProduct.setCategory(request.getCategory());

        Product updatdeProduct = productRepository.save(oldProduct);

        return mapToResponse(updatdeProduct);
    }

    public String deleteProduct(String id){

        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
        return "Product with id "+id+" deleted successfully";
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
        response.setCreatedAt(product.getCreatedAt());

        return response;
    }
}
