package com.koramarket.order.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class InvoiceResponseDTO {
    private UUID id;
    private UUID orderId;
    private String invoiceNumber;
    private String urlPdf;
    private String status;
    private Instant issuedAt;

    // bonus lecture
    private Long totalAmount;
    private String currency;
}
