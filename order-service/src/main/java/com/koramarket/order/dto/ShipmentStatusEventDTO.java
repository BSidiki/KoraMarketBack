package com.koramarket.order.dto;
import jakarta.validation.constraints.*; import lombok.Data;

@Data
public class ShipmentStatusEventDTO {
    @NotNull private String status;   // CREATED|PACKED|SHIPPED|DELIVERED|CANCELED
    private String carrier;
    private String trackingNumber;
}
