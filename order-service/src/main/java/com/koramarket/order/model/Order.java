package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name="orders")
public class Order {

    @Id
    @Column(columnDefinition = "uuid")
    @EqualsAndHashCode.Include @ToString.Include
    private UUID id;

    @Column(name="order_number", length=32, unique = true, nullable = false)
    private String orderNumber;

    @Column(name="user_id_ext", columnDefinition = "uuid", nullable = false)
    private UUID userIdExt;

    @Column(length=3, nullable=false)
    private String currency; // ex: XOF

    @Column(name="subtotal_amount", nullable=false)
    private Long subtotalAmount; // centimes

    @Column(name="tax_total_amount", nullable=false)
    private Long taxTotalAmount;

    @Column(name="shipping_total_amount", nullable=false)
    private Long shippingTotalAmount;

    @Column(name="discount_total_amount", nullable=false)
    private Long discountTotalAmount;

    @Column(name="grand_total_amount", nullable=false)
    private Long grandTotalAmount;

    @Column(name="order_status", length=32, nullable=false)
    private String orderStatus;

    @Column(name="payment_status", length=32, nullable=false)
    private String paymentStatus;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        var now = Instant.now();
        createdAt = now; updatedAt = now;
        if (version == null) version = 0;
    }
    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }
}
