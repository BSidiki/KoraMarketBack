package com.koramarket.order.mapper;

import com.koramarket.order.dto.InvoiceResponseDTO;
import com.koramarket.order.model.Invoice;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InvoiceMapper {
    public static InvoiceResponseDTO toResponse(Invoice i) {
        var dto = new InvoiceResponseDTO();
        dto.setId(i.getId());
        dto.setOrderId(i.getOrder().getId());
        dto.setInvoiceNumber(i.getInvoiceNumber());
        dto.setUrlPdf(i.getUrlPdf());
        dto.setStatus(i.getStatus().name());
        dto.setIssuedAt(i.getIssuedAt());

        // infos utiles
        var o = i.getOrder();
        dto.setTotalAmount(o.getGrandTotalAmount());
        dto.setCurrency(o.getCurrency());
        return dto;
    }
}
