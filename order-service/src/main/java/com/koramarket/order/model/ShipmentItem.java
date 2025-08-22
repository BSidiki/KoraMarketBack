// ShipmentItem.java
package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "shipment_items", schema = "order_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentItem {
    @Id @Column(columnDefinition = "uuid") private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="shipment_id", nullable=false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="order_item_id", nullable=false)
    private OrderItem orderItem;

    @Column(nullable=false)
    private Integer quantity;

    @PrePersist void pp(){ if(id==null) id=UUID.randomUUID(); }
}
