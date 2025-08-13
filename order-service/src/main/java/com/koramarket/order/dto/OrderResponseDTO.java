package com.koramarket.order.dto;

import lombok.Data;
import java.time.Instant;
import java.util.*;

@Data
public class OrderResponseDTO {
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private String vendeurEmail;
    private UUID vendeurId;
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
}
