package com.sarthak.ecommerce.cart.controller;

import com.sarthak.ecommerce.cart.dto.CartItemRequest;
import com.sarthak.ecommerce.cart.dto.CartResponse;
import com.sarthak.ecommerce.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart();
    }

    @PostMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(
            @PathVariable String productId,
            @Valid @RequestBody CartItemRequest request
    ) {
        return cartService.addItem(
                productId,
                request
        );
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(
            @PathVariable String productId,
            @Valid @RequestBody CartItemRequest request
    ) {
        return cartService.updateItem(
                productId,
                request
        );
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            @PathVariable String productId
    ) {
        return cartService.removeItem(productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        cartService.clearCart();
    }
}