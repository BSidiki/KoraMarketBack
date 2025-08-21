package com.koramarket.product.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_reservation_items", schema = "product_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="reservation_id", nullable=false) private InventoryReservation reservation;
    @Column(name="product_id", nullable=false) private Long productId;
    @Column(nullable=false) private Integer quantity;
}
