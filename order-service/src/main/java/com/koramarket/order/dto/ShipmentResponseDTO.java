package com.koramarket.order.dto;
import lombok.Data; import java.time.Instant; import java.util.*;

@Data
public class ShipmentResponseDTO {
    private UUID id; private UUID orderId; private UUID vendorId;
    private String carrier; private String trackingNumber; private String status;
    private Instant createdAt; private Instant shippedAt; private Instant deliveredAt;
    private java.util.List<Item> items;

    @Data public static class Item { private UUID orderItemId; private int quantity; }
}
