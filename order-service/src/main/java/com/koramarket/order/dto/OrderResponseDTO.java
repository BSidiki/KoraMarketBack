package com.koramarket.order.dto;

import lombok.Data;
import java.time.Instant;
import java.util.*;

// OrderResponseDTO.java
@Data
public class OrderResponseDTO {
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String currency;
    private Long subtotal;
    private Long taxTotal;
    private Long shippingTotal;
    private Long discountTotal;
    private Long grandTotal;
    private String orderStatus;
    private String paymentStatus;
    private Instant createdAt;
    private List<Item> items;

    // --- Compat : remplis SEULEMENT si un unique vendeur, sinon null ---
    private String vendeurEmail;
    private UUID vendeurId;

    // --- Recommandé : vue agrégée par vendeur ---
    private Integer vendorCount;
    private List<VendorSummary> vendors;

    @Data public static class Item {
        private UUID id;
        private Long productId;
        private String name;
        private String sku;
        private Long unitPrice;
        private Integer quantity;
        private Long taxAmount;
        private Long lineTotal;
        private String image;
    }

    @Data public static class VendorSummary {
        private UUID vendorId;
        private String vendorEmail;
        private Integer itemCount;     // somme des quantités
        private Long subtotal;         // somme(unitPrice * qty), hors taxes
        private Long taxTotal;         // somme(taxAmount)
        private Long grandTotal;       // subtotal + taxTotal (hors shipping/discount)
    }
}

