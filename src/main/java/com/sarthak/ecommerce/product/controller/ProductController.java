package com.sarthak.ecommerce.product.controller;

import com.sarthak.ecommerce.common.dto.PageResponse;
import com.sarthak.ecommerce.common.response.ApiResponse;
import com.sarthak.ecommerce.product.dto.ProductRequest;
import com.sarthak.ecommerce.product.dto.ProductResponse;
import com.sarthak.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public PageResponse<ProductResponse> getAllProducts(@PageableDefault(size = 10,sort = "price",direction = Sort.Direction.ASC) Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/search")
    public PageResponse<ProductResponse> getAllProductsByName(@RequestParam String name,@PageableDefault(size = 10,sort = "price",direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.searchProducts(name,pageable);
    }

    @GetMapping("/filter")
    public PageResponse<ProductResponse> getFilteredProducts(@RequestParam BigDecimal minPrice, @RequestParam BigDecimal maxPrice, @PageableDefault(size = 10,sort = "price",direction = Sort.Direction.ASC) Pageable pageable ) {
        return  productService.getFilteredProducts(minPrice,maxPrice,pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable String id){
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request){
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request){
        return  productService.updateProduct(id,request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id){
        productService.deleteProduct(id);
    }
}
