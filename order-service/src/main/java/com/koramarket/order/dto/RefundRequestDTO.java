package com.koramarket.order.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RefundRequestDTO {
    private UUID paymentId;           // requis
    private Long amount;              // si null => full refund (reste capturé non remboursé)
    private String reason;            // optionnel
}
