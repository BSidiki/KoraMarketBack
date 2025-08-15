package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "order_service")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "invoice_number", length = 32, nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "url_pdf")
    private String urlPdf;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private InvoiceStatus status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (issuedAt == null) issuedAt = Instant.now();
        if (status == null) status = InvoiceStatus.ISSUED;
    }
}
