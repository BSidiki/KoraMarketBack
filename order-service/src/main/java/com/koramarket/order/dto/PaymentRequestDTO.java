package com.koramarket.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentRequestDTO {
    @NotNull
    private UUID orderId;

    private String provider;   // "MOCK" par défaut si null
    private String currency;   // optionnel: sinon celui de la commande
}
