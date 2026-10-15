package com.sarthak.ecommerce.cart.service;

import com.sarthak.ecommerce.cart.dto.CartItemRequest;
import com.sarthak.ecommerce.cart.dto.CartItemResponse;
import com.sarthak.ecommerce.cart.dto.CartResponse;
import com.sarthak.ecommerce.cart.exception.CartNotFoundException;
import com.sarthak.ecommerce.cart.exception.InsufficientStockException;
import com.sarthak.ecommerce.cart.model.Cart;
import com.sarthak.ecommerce.cart.model.CartItem;
import com.sarthak.ecommerce.cart.repository.CartRepository;
import com.sarthak.ecommerce.product.exception.ProductNotFoundException;
import com.sarthak.ecommerce.product.model.Product;
import com.sarthak.ecommerce.product.repository.ProductRepository;
import com.sarthak.ecommerce.user.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserUtil userUtil;

    public CartResponse getCart() {

        String userId = userUtil.getAuthenticatedUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        return mapToResponse(cart);
    }

    public CartResponse addItem(
            String productId,
            CartItemRequest request
    ) {

        String userId = userUtil.getAuthenticatedUserId();

        Product product = getProduct(productId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity() + request.quantity();

            validateStock(product, newQuantity);

            existingItem.setQuantity(newQuantity);

        } else {

            validateStock(product, request.quantity());

            cart.getItems().add(
                    new CartItem(
                            productId,
                            request.quantity()
                    )
            );
        }

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        return mapToResponse(savedCart);
    }

    public CartResponse updateItem(
            String productId,
            CartItemRequest request
    ) {

        String userId = userUtil.getAuthenticatedUserId();

        Product product = getProduct(productId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found"
                        )
                );

        CartItem item = cart.getItems()
                .stream()
                .filter(cartItem ->
                        cartItem.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Product is not present in cart"
                        )
                );

        validateStock(product, request.quantity());

        item.setQuantity(request.quantity());

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        return mapToResponse(savedCart);
    }

    public CartResponse removeItem(String productId) {

        String userId = userUtil.getAuthenticatedUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found"
                        )
                );

        boolean removed = cart.getItems()
                .removeIf(item ->
                        item.getProductId().equals(productId));

        if (!removed) {
            throw new CartNotFoundException(
                    "Product is not present in cart"
            );
        }

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        return mapToResponse(savedCart);
    }

    public void clearCart() {

        String userId = userUtil.getAuthenticatedUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found"
                        )
                );

        cart.setItems(new ArrayList<>());
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }

    private Cart createCart(String userId) {

        Cart cart = new Cart();

        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());

        LocalDateTime now = LocalDateTime.now();

        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);

        return cartRepository.save(cart);
    }

    private Product getProduct(String productId) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );
    }

    private void validateStock(
            Product product,
            int requestedQuantity
    ) {

        if (product.getStock() == null ||
                requestedQuantity > product.getStock()) {

            throw new InsufficientStockException(
                    "Requested quantity exceeds available stock"
            );
        }
    }

    private CartResponse mapToResponse(Cart cart) {

        List<CartItemResponse> itemResponses =
                cart.getItems()
                        .stream()
                        .map(this::mapItemToResponse)
                        .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                itemResponses,
                total
        );
    }

    private CartItemResponse mapItemToResponse(
            CartItem cartItem
    ) {

        Product product = getProduct(cartItem.getProductId());

        BigDecimal subtotal =
                product.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        cartItem.getQuantity()
                                )
                        );

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                subtotal
        );
    }
}