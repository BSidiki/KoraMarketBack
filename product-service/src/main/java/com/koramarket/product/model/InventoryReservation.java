package com.koramarket.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations", schema = "product_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservation {
    @Id
    @Column(columnDefinition = "uuid") private UUID id;
    @Column(name="order_id", columnDefinition = "uuid", nullable=false, unique = true) private UUID orderId;
    @Column(length=16, nullable=false) private String status; // ACTIVE | RELEASED
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="released_at") private Instant releasedAt;

    @OneToMany(mappedBy="reservation", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<InventoryReservationItem> items = new ArrayList<>();

    @PrePersist void pp(){ if(id==null) id=UUID.randomUUID(); if(createdAt==null) createdAt=Instant.now(); if(status==null) status="ACTIVE"; }
}


