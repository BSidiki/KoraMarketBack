package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds", schema = "order_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Refund {
    @Id @Column(columnDefinition = "uuid") private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false) private Long amount;
    @Column(nullable = false, length = 3) private String currency;

    @Enumerated(EnumType.STRING) @Column(length=32, nullable=false)
    private RefundStatus status;

    @Column(name="external_refund_id", unique = true)
    private String externalRefundId;

    private String reason;

    @Column(name="created_at", nullable=false)  private Instant createdAt;
    @Column(name="completed_at")                private Instant completedAt;

    @PrePersist void pp(){ if(id==null) id=UUID.randomUUID(); if(createdAt==null) createdAt=Instant.now(); }
}
