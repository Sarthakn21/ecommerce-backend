package com.sarthak.ecommerce.order.service;

import com.sarthak.ecommerce.cart.exception.CartNotFoundException;
import com.sarthak.ecommerce.cart.exception.InsufficientStockException;
import com.sarthak.ecommerce.cart.model.Cart;
import com.sarthak.ecommerce.cart.model.CartItem;
import com.sarthak.ecommerce.cart.repository.CartRepository;
import com.sarthak.ecommerce.common.exception.ApiException;
import com.sarthak.ecommerce.order.dto.OrderItemResponse;
import com.sarthak.ecommerce.order.dto.OrderResponse;
import com.sarthak.ecommerce.order.model.Order;
import com.sarthak.ecommerce.order.model.OrderItem;
import com.sarthak.ecommerce.order.model.OrderStatus;
import com.sarthak.ecommerce.order.repository.OrderRepository;
import com.sarthak.ecommerce.product.exception.ProductNotFoundException;
import com.sarthak.ecommerce.product.model.Product;
import com.sarthak.ecommerce.product.repository.ProductRepository;
import com.sarthak.ecommerce.user.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserUtil userUtil;


    public OrderResponse placeOrder() {

        String userId = userUtil.getAuthenticatedUserId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException("Cart not found")
                );

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot place an order with an empty cart"
            );
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Build order snapshots using current product details.
        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository.findById(
                    cartItem.getProductId()
            ).orElseThrow(() ->
                    new ProductNotFoundException(cartItem.getProductId())
            );

            if (product.getPrice() == null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Product price is unavailable: " + product.getName()
                );
            }

            BigDecimal subtotal = product.getPrice().multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    cartItem.getQuantity(),
                    subtotal
            );

            orderItems.add(orderItem);
            total = total.add(subtotal);
        }

        // Reserve stock atomically, one product at a time.
        List<OrderItem> reservedItems = new ArrayList<>();
        Order savedOrder = null;

        try {

            for (OrderItem item : orderItems) {

                Query query = new Query(
                        Criteria.where("_id").is(item.getProductId())
                                .and("stock").gte(item.getQuantity())
                );

                Update update = new Update()
                        .inc("stock", -item.getQuantity());

                Product updatedProduct = mongoTemplate.findAndModify(
                        query,
                        update,
                        FindAndModifyOptions.options().returnNew(true),
                        Product.class
                );

                if (updatedProduct == null) {
                    throw new InsufficientStockException(
                            "Insufficient stock for product: "
                                    + item.getProductName()
                    );
                }

                reservedItems.add(item);
            }

            // Create and save the order.
            LocalDateTime now = LocalDateTime.now();

            Order order = new Order();
            order.setUserId(userId);
            order.setItems(orderItems);
            order.setTotal(total);
            order.setStatus(OrderStatus.PLACED);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            savedOrder = orderRepository.save(order);

            // Clear the cart only after the order is saved.
            cart.setItems(new ArrayList<>());
            cart.setUpdatedAt(now);
            cartRepository.save(cart);

            return mapToResponse(savedOrder);

        } catch (RuntimeException exception) {

            // Compensate for stock deductions if checkout fails.
            if (savedOrder != null) {
                orderRepository.deleteById(savedOrder.getId());
            }

            restoreStock(reservedItems);

            throw exception;
        }
    }


    public List<OrderResponse> getMyOrders() {

        String userId = userUtil.getAuthenticatedUserId();

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public OrderResponse getOrderById(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        userUtil.verifyUserOwnership(order.getUserId());

        return mapToResponse(order);
    }


    public OrderResponse cancelOrder(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"
                        )
                );

        userUtil.verifyUserOwnership(order.getUserId());

        if (order.getStatus() != OrderStatus.PLACED
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "This order cannot be cancelled"
            );
        }

        // Restore the stock of every ordered product.
        for (OrderItem item : order.getItems()) {

            Query query = new Query(
                    Criteria.where("_id").is(item.getProductId())
            );

            Update update = new Update()
                    .inc("stock", item.getQuantity());

            mongoTemplate.updateFirst(query, update, Product.class);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }


    private void restoreStock(List<OrderItem> items) {

        for (OrderItem item : items) {

            Query query = new Query(
                    Criteria.where("_id").is(item.getProductId())
            );

            Update update = new Update()
                    .inc("stock", item.getQuantity());

            mongoTemplate.updateFirst(query, update, Product.class);
        }
    }


    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                itemResponses,
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}