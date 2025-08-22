package com.koramarket.order.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class CreditNoteResponseDTO {
    private UUID id;
    private UUID refundId;
    private UUID orderId;
    private UUID invoiceId;
    private String creditNumber;
    private String urlPdf;
    private String status;
    private Long amount;
    private String currency;
    private String reason;
    private Instant createdAt;
    private Instant issuedAt;
}
