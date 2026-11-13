package com.sarthak.ecommerce.seller.service;

import com.sarthak.ecommerce.common.exception.ApiException;
import com.sarthak.ecommerce.order.dto.OrderItemResponse;
import com.sarthak.ecommerce.order.dto.SellerOrderResponse;
import com.sarthak.ecommerce.order.dto.UpdateOrderItemStatusRequest;
import com.sarthak.ecommerce.order.model.Order;
import com.sarthak.ecommerce.order.model.OrderItem;
import com.sarthak.ecommerce.order.model.OrderItemStatus;
import com.sarthak.ecommerce.order.model.OrderStatus;
import com.sarthak.ecommerce.order.repository.OrderRepository;
import com.sarthak.ecommerce.product.dto.ProductResponse;
import com.sarthak.ecommerce.product.service.ProductService;
import com.sarthak.ecommerce.user.util.UserUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SellerService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserUtil userUtil;
    private final MongoTemplate mongoTemplate;

    public SellerService(
            OrderRepository orderRepository,
            ProductService productService,
            UserUtil userUtil,
            MongoTemplate mongoTemplate
    ) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.userUtil = userUtil;
        this.mongoTemplate = mongoTemplate;
    }

    public List<ProductResponse> getMyProducts() {
        return productService.getMyProducts();
    }

    public ProductResponse deactivateProduct(String productId) {
        return productService.deactivateProduct(productId);
    }

    public List<SellerOrderResponse> getMyOrders() {
        String sellerId = userUtil.getAuthenticatedUserId();
        return orderRepository.findByItemsSellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(order -> toSellerOrder(order, sellerId))
                .filter(response -> !response.items().isEmpty())
                .toList();
    }

    public SellerOrderResponse updateItemStatus(
            String orderId, String itemId, UpdateOrderItemStatusRequest request
    ) {
        String sellerId = userUtil.getAuthenticatedUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderItem item = order.getItems().stream()
                .filter(candidate -> Objects.equals(candidate.getItemId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));

        if (!Objects.equals(sellerId, item.getSellerId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You do not have permission to update this order item");
        }

        OrderItemStatus currentStatus = resolveStatus(item, order.getStatus());
        OrderItemStatus nextStatus = request.status();
        if (!isAllowedTransition(currentStatus, nextStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Invalid order item status transition from " + currentStatus + " to " + nextStatus);
        }

        if (nextStatus == OrderItemStatus.CANCELLED) {
            if (request.reason() == null || request.reason().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "A cancellation reason is required");
            }
            restoreStock(item);
            item.setCancellationReason(request.reason().trim());
        }

        item.setStatus(nextStatus);
        order.setStatus(calculateOrderStatus(order));
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        return toSellerOrder(savedOrder, sellerId);
    }

    private boolean isAllowedTransition(OrderItemStatus current, OrderItemStatus next) {
        if (current == null || next == null || current == next) return false;
        return switch (current) {
            case PLACED -> next == OrderItemStatus.CONFIRMED || next == OrderItemStatus.CANCELLED;
            case CONFIRMED -> next == OrderItemStatus.PROCESSING || next == OrderItemStatus.CANCELLED;
            case PROCESSING -> next == OrderItemStatus.SHIPPED || next == OrderItemStatus.CANCELLED;
            case SHIPPED -> next == OrderItemStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private void restoreStock(OrderItem item) {
        Query query = new Query(Criteria.where("_id").is(item.getProductId()));
        Update update = new Update().inc("stock", item.getQuantity());
        mongoTemplate.updateFirst(query, update, com.sarthak.ecommerce.product.model.Product.class);
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

    private OrderStatus calculateOrderStatus(Order order) {
        List<OrderItemStatus> statuses = order.getItems().stream()
                .map(item -> resolveStatus(item, order.getStatus()))
                .toList();

        if (statuses.stream().allMatch(status -> status == OrderItemStatus.CANCELLED)) {
            return OrderStatus.CANCELLED;
        }
        if (statuses.stream().allMatch(status -> status == OrderItemStatus.DELIVERED)) {
            return OrderStatus.DELIVERED;
        }
        if (statuses.stream().anyMatch(status -> status == OrderItemStatus.CANCELLED)) {
            return OrderStatus.PARTIALLY_FULFILLED;
        }
        if (statuses.stream().anyMatch(status -> status == OrderItemStatus.PROCESSING)) {
            return OrderStatus.PROCESSING;
        }
        if (statuses.stream().anyMatch(status -> status == OrderItemStatus.SHIPPED)) {
            return OrderStatus.SHIPPED;
        }
        if (statuses.stream().anyMatch(status -> status == OrderItemStatus.CONFIRMED)) {
            return OrderStatus.CONFIRMED;
        }
        return OrderStatus.PLACED;
    }

    private SellerOrderResponse toSellerOrder(Order order, String sellerId) {
        List<OrderItemResponse> items = order.getItems().stream()
                .filter(item -> Objects.equals(sellerId, item.getSellerId()))
                .map(item -> new OrderItemResponse(
                        item.getItemId(), item.getProductId(), item.getSellerId(),
                        item.getProductName(), item.getPrice(), item.getQuantity(),
                        item.getSubtotal(), resolveStatus(item, order.getStatus()),
                        item.getCancellationReason()
                )).toList();

        return new SellerOrderResponse(order.getId(), order.getUserId(), order.getStatus(),
                items, order.getCreatedAt(), order.getUpdatedAt());
    }
}
