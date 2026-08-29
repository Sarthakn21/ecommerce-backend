package com.sarthak.ecommerce.product.controller;

import com.sarthak.ecommerce.product.dto.ProductRequest;
import com.sarthak.ecommerce.product.dto.ProductResponse;
import com.sarthak.ecommerce.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable String id){
        return productService.getProductById(id);
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest request){
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable String id,@RequestBody ProductRequest request){
        return  productService.updateProduct(id,request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id){
        String response = productService.deleteProduct(id);
        return ResponseEntity.ok(response);
    }
}
