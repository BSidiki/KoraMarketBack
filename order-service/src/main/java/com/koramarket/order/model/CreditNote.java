// CreditNote.java
package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_notes", schema = "order_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreditNote {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id") private Invoice invoice;

    @Column(name="credit_number", length=32, unique = true, nullable = false)
    private String creditNumber;

    @Column(name="amount", nullable=false) private Long amount; // centimes
    @Column(name="currency", length=3, nullable=false) private String currency;

    @Column(name="created_at", nullable=false) private Instant createdAt;

    @PrePersist void pp() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}
