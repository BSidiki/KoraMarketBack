package com.koramarket.order.repository;

import com.koramarket.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderId(UUID orderId);
}
