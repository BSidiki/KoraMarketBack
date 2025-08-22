package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "credit_notes",
        schema = "order_service",
        uniqueConstraints = @UniqueConstraint(name = "uk_credit_notes_refund", columnNames = "refund_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreditNote {
    @Id @Column(columnDefinition = "uuid") private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="refund_id", nullable=false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="order_id", nullable=false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="invoice_id")
    private Invoice invoice; // optionnel : l'avoir “référence” la facture

    @Column(name="credit_number", unique = true, nullable=false, length=64)
    private String creditNumber;

    @Column(name="url_pdf")
    private String urlPdf;

    @Enumerated(EnumType.STRING) @Column(length=32, nullable=false)
    private CreditNoteStatus status;

    @Column(nullable=false) private Long amount;
    @Column(nullable=false, length=3) private String currency;
    private String reason;

    @Column(name="created_at", nullable=false)  private Instant createdAt;
    @Column(name="issued_at")                   private Instant issuedAt;

    @PrePersist
    void pp() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = CreditNoteStatus.ISSUED;      // 👍
        if (issuedAt == null) issuedAt = Instant.now();            // 👍
    }

}
