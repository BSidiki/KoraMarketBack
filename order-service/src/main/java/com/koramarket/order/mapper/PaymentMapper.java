package com.koramarket.order.mapper;

import com.koramarket.order.dto.PaymentResponseDTO;
import com.koramarket.order.model.Payment;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PaymentMapper {

    public static PaymentResponseDTO toResponse(Payment p) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(p.getId());
        dto.setOrderId(p.getOrder().getId());
        dto.setProvider(p.getProvider());
        dto.setStatus(p.getStatus());
        dto.setAmountAuthorized(p.getAmountAuthorized());
        dto.setAmountCaptured(p.getAmountCaptured());
        dto.setCurrency(p.getCurrency());
        dto.setExternalTransactionId(p.getExternalTransactionId());
        dto.setFailureReason(p.getFailureReason());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setAuthorizedAt(p.getAuthorizedAt());
        dto.setCapturedAt(p.getCapturedAt());
        return dto;
    }
}
