package com.koramarket.order.mapper;

import com.koramarket.order.dto.ShipmentResponseDTO;
import com.koramarket.order.model.Shipment;
import com.koramarket.order.model.ShipmentItem;
import lombok.experimental.UtilityClass;
import java.util.stream.Collectors;

@UtilityClass
public class ShipmentMapper {
    public ShipmentResponseDTO toResponse(Shipment s){
        var dto = new ShipmentResponseDTO();
        dto.setId(s.getId());
        dto.setOrderId(s.getOrder().getId());
        dto.setVendorId(s.getVendorIdExt());
        dto.setCarrier(s.getCarrier());
        dto.setTrackingNumber(s.getTrackingNumber());
        dto.setStatus(s.getStatus().name());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setShippedAt(s.getShippedAt());
        dto.setDeliveredAt(s.getDeliveredAt());
        dto.setItems(s.getItems().stream().map(ShipmentMapper::toItem).collect(Collectors.toList()));
        return dto;
    }
    private ShipmentResponseDTO.Item toItem(ShipmentItem it){
        var dto = new ShipmentResponseDTO.Item();
        dto.setOrderItemId(it.getOrderItem().getId());
        dto.setQuantity(it.getQuantity());
        return dto;
    }
}
