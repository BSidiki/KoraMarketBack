package com.koramarket.order.dto;

import com.koramarket.order.model.PaymentState;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentResponseDTO {
    private UUID id;
    private UUID orderId;
    private String provider;
    private PaymentState status;
    private Long amountAuthorized;
    private Long amountCaptured;
    private String currency;
    private String externalTransactionId;
    private String failureReason;
    private Instant createdAt;
    private Instant authorizedAt;
    private Instant capturedAt;
}
