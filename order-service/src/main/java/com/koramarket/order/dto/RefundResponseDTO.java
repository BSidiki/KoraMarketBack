package com.koramarket.order.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class RefundResponseDTO {
    private UUID id;
    private UUID paymentId;
    private UUID orderId;
    private Long amount;
    private String currency;
    private String status;
    private String externalRefundId;
    private String reason;
    private Instant createdAt;
    private Instant completedAt;
}
