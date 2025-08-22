// ShipmentRepository.java
package com.koramarket.order.repository;

import com.koramarket.order.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.*;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    List<Shipment> findByOrder_IdOrderByCreatedAtAsc(UUID orderId);

    @Query("""
      select si.orderItem.id, sum(si.quantity)
      from ShipmentItem si
      where si.shipment.order.id = :orderId
      group by si.orderItem.id
    """)
    List<Object[]> sumQuantitiesByOrderItem(UUID orderId);
}
