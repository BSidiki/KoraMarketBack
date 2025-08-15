package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "uk_payments_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "uk_payments_ext_tx", columnList = "external_transaction_id", unique = true),
                @Index(name = "idx_payments_order_id", columnList = "order_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(length = 64, nullable = false)
    private String provider;           // "MOCK", "STRIPE", "PAYSTACK", ...

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private PaymentState status;       // CREATED/AUTHORIZED/CAPTURED/...

    @Column(name = "amount_authorized", nullable = false)
    private Long amountAuthorized;     // centimes

    @Column(name = "amount_captured", nullable = false)
    private Long amountCaptured;       // centimes

    @Column(length = 3, nullable = false)
    private String currency;           // ex: XOF

    @Column(name = "external_transaction_id", length = 128)
    private String externalTransactionId;

    @Column(name = "idempotency_key", length = 80)
    private String idempotencyKey;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (amountCaptured == null) amountCaptured = 0L;
        if (status == null) status = PaymentState.CREATED;
    }
}
