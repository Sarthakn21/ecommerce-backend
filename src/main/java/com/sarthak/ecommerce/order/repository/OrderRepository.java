package com.sarthak.ecommerce.order.repository;
import com.sarthak.ecommerce.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Order> findAllByOrderByCreatedAtDesc();
}
