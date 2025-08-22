package com.koramarket.order.dto;
import jakarta.validation.constraints.*; import lombok.Data;
import java.util.*;

@Data
public class ShipmentRequestDTO {
    @NotNull private UUID orderId;
    private UUID vendorId;              // optionnel (peut s’inférer via items)
    private String carrier;
    private String trackingNumber;
    @NotEmpty private List<Line> items;

    @Data public static class Line { @NotNull private UUID orderItemId; @Min(1) private int quantity; }
}
