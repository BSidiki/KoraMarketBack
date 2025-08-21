package com.koramarket.product.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class ReserveStockResponseDTO {
    private UUID reservationId; private UUID orderId; private String status; // ACTIVE | RELEASED
    private List<Line> lines;
    @Data public static class Line { private Long productId; private int quantity; }
}
