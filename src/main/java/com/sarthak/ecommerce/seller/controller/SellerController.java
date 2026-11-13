package com.sarthak.ecommerce.seller.controller;

import com.sarthak.ecommerce.order.dto.SellerOrderResponse;
import com.sarthak.ecommerce.order.dto.UpdateOrderItemStatusRequest;
import com.sarthak.ecommerce.product.dto.ProductResponse;
import com.sarthak.ecommerce.seller.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getMyProducts() {
        return ResponseEntity.ok(sellerService.getMyProducts());
    }

    @PatchMapping("/products/{productId}/deactivate")
    public ResponseEntity<ProductResponse> deactivateProduct(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(sellerService.deactivateProduct(productId));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<SellerOrderResponse>> getMyOrders() {
        return ResponseEntity.ok(sellerService.getMyOrders());
    }

    @PatchMapping("/orders/{orderId}/items/{itemId}/status")
    public ResponseEntity<SellerOrderResponse> updateItemStatus(
            @PathVariable String orderId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateOrderItemStatusRequest request
    ) {
        return ResponseEntity.ok(
                sellerService.updateItemStatus(orderId, itemId, request));
    }
}
