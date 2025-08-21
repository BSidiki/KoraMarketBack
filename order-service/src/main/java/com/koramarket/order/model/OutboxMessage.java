// OutboxMessage.java
package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages", schema = "order_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxMessage {
    @Id @Column(columnDefinition = "uuid") private UUID id;

    @Column(name="aggregate_id", columnDefinition = "uuid", nullable = false)
    private UUID aggregateId;

    @Column(length = 100, nullable = false)
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private java.util.Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OutboxStatus status;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @Column(name="sent_at")
    private Instant sentAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name="last_error")
    private String lastError;

    @PrePersist
    void pp() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = OutboxStatus.PENDING;
        // attempts à 0 par défaut
    }
}
