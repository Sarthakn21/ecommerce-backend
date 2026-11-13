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
import com.sarthak.ecommerce.order.model.OrderItemStatus;
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
import java.util.UUID;

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
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot place an order with an empty cart");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(cartItem.getProductId()));

            if (!product.isActive()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "This product is no longer available: " + product.getName());
            }
            if (product.getPrice() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Product price is unavailable: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = new OrderItem(
                    UUID.randomUUID().toString(),
                    product.getId(),
                    product.getSellerId(),
                    product.getName(),
                    product.getPrice(),
                    cartItem.getQuantity(),
                    subtotal,
                    OrderItemStatus.PLACED,
                    null
            );
            orderItems.add(orderItem);
            total = total.add(subtotal);
        }

        List<OrderItem> reservedItems = new ArrayList<>();
        Order savedOrder = null;
        try {
            for (OrderItem item : orderItems) {
                Query query = new Query(Criteria.where("_id").is(item.getProductId())
                        .and("stock").gte(item.getQuantity())
                        .and("active").ne(false));
                Update update = new Update().inc("stock", -item.getQuantity());
                Product updatedProduct = mongoTemplate.findAndModify(
                        query, update, FindAndModifyOptions.options().returnNew(true),
                        Product.class);
                if (updatedProduct == null) {
                    throw new InsufficientStockException(
                            "Insufficient stock or product unavailable: " + item.getProductName());
                }
                reservedItems.add(item);
            }

            LocalDateTime now = LocalDateTime.now();
            Order order = new Order();
            order.setUserId(userId);
            order.setItems(orderItems);
            order.setTotal(total);
            order.setStatus(OrderStatus.PLACED);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            savedOrder = orderRepository.save(order);

            cart.setItems(new ArrayList<>());
            cart.setUpdatedAt(now);
            cartRepository.save(cart);
            return mapToResponse(savedOrder);
        } catch (RuntimeException exception) {
            if (savedOrder != null) {
                orderRepository.deleteById(savedOrder.getId());
            }
            restoreStock(reservedItems);
            throw exception;
        }
    }

    public List<OrderResponse> getMyOrders() {
        String userId = userUtil.getAuthenticatedUserId();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = findOrder(orderId);
        userUtil.verifyUserOwnership(order.getUserId());
        return mapToResponse(order);
    }

    public OrderResponse cancelOrder(String orderId) {
        Order order = findOrder(orderId);
        userUtil.verifyUserOwnership(order.getUserId());

        if (order.getItems().stream().anyMatch(item ->
                !isCancellable(resolveStatus(item, order.getStatus())))) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This order contains an item that can no longer be cancelled");
        }

        for (OrderItem item : order.getItems()) {
            if (resolveStatus(item, order.getStatus()) != OrderItemStatus.CANCELLED) {
                restoreStock(item);
                item.setStatus(OrderItemStatus.CANCELLED);
                item.setCancellationReason("Cancelled by customer");
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(orderRepository.save(order));
    }

    private Order findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private boolean isCancellable(OrderItemStatus status) {
        return status == OrderItemStatus.PLACED
                || status == OrderItemStatus.CONFIRMED
                || status == OrderItemStatus.PROCESSING
                || status == OrderItemStatus.CANCELLED;
    }

    private OrderItemStatus resolveStatus(OrderItem item, OrderStatus orderStatus) {
        if (item.getStatus() != null) return item.getStatus();
        if (orderStatus == OrderStatus.CANCELLED) return OrderItemStatus.CANCELLED;
        if (orderStatus == OrderStatus.DELIVERED) return OrderItemStatus.DELIVERED;
        if (orderStatus == OrderStatus.SHIPPED) return OrderItemStatus.SHIPPED;
        if (orderStatus == OrderStatus.PROCESSING) return OrderItemStatus.PROCESSING;
        if (orderStatus == OrderStatus.CONFIRMED) return OrderItemStatus.CONFIRMED;
        return OrderItemStatus.PLACED;
    }

    private void restoreStock(OrderItem item) {
        Query query = new Query(Criteria.where("_id").is(item.getProductId()));
        Update update = new Update().inc("stock", item.getQuantity());
        mongoTemplate.updateFirst(query, update, Product.class);
    }

    private void restoreStock(List<OrderItem> items) {
        items.forEach(this::restoreStock);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getItemId(),
                        item.getProductId(),
                        item.getSellerId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        resolveStatus(item, order.getStatus()),
                        item.getCancellationReason()
                )).toList();

        return new OrderResponse(order.getId(), order.getUserId(), itemResponses,
                order.getTotal(), order.getStatus(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
