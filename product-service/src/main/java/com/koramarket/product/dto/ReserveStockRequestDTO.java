package com.koramarket.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReserveStockRequestDTO {
    @NotNull
    private UUID orderId;
    @NotEmpty
    private List<Line> lines;
    @Data public static class Line { @NotNull private Long productId; @Min(1) private int quantity; }
}
