package com.koramarket.order.repository;

import com.koramarket.order.model.Order;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    @Query("select o from Order o left join fetch o.items where o.id = :id")
    Optional<Order> findOneWithItems(UUID id);
    List<Order> findByUserIdExtOrderByCreatedAtDesc(UUID userIdExt);
}
