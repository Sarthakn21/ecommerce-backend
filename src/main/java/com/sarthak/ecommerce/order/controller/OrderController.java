package com.sarthak.ecommerce.order.controller;

import com.sarthak.ecommerce.order.dto.OrderResponse;
import com.sarthak.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Place a new order
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.placeOrder());
    }

    // Get all orders of the logged-in user
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    // Get order details
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable String orderId) {

        return ResponseEntity.ok(
                orderService.getOrderById(orderId)
        );
    }

    // Cancel an order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderId) {

        return ResponseEntity.ok(
                orderService.cancelOrder(orderId)
        );
    }
}