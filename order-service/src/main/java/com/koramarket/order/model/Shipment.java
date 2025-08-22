// Shipment.java
package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "shipments", schema = "order_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {
    @Id @Column(columnDefinition = "uuid") private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "vendor_id_ext", columnDefinition = "uuid")
    private UUID vendorIdExt;

    private String carrier;
    @Column(name="tracking_number")
    private String trackingNumber;

    @Enumerated(EnumType.STRING) @Column(length = 32, nullable = false)
    private ShipmentStatus status;

    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    @Column(name="shipped_at") private Instant shippedAt;
    @Column(name="delivered_at") private Instant deliveredAt;

    @Version private Integer version;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items = new ArrayList<>();

    @PrePersist void pp(){ if(id==null) id=UUID.randomUUID(); var now=Instant.now(); createdAt=now; updatedAt=now; if(status==null) status=ShipmentStatus.CREATED; if(version==null) version=0; }
    @PreUpdate void pu(){ updatedAt = Instant.now(); }
}
