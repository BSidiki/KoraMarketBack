package com.koramarket.order.mapper;

import com.koramarket.order.dto.OrderAddressRequestDTO;
import com.koramarket.order.dto.OrderAddressResponseDTO;
import com.koramarket.order.model.OrderAddress;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderAddressMapper {

    public static void apply(OrderAddressRequestDTO src, OrderAddress tgt) {
        // upsert/patch
        tgt.setAddrType(src.getAddrType());
        tgt.setFullName(nvl(src.getFullName()));
        tgt.setPhone(nvl(src.getPhone()));
        tgt.setLine1(nvl(src.getLine1()));
        tgt.setLine2(nvl(src.getLine2()));
        tgt.setCity(nvl(src.getCity()));
        tgt.setState(nvl(src.getState()));
        tgt.setPostalCode(nvl(src.getPostalCode()));
        tgt.setCountry(nvl(src.getCountry()));
    }

    public static OrderAddressResponseDTO toResponse(OrderAddress a) {
        OrderAddressResponseDTO dto = new OrderAddressResponseDTO();
        dto.setId(a.getId());
        dto.setAddrType(a.getAddrType());
        dto.setFullName(a.getFullName());
        dto.setPhone(a.getPhone());
        dto.setLine1(a.getLine1());
        dto.setLine2(a.getLine2());
        dto.setCity(a.getCity());
        dto.setState(a.getState());
        dto.setPostalCode(a.getPostalCode());
        dto.setCountry(a.getCountry());
        return dto;
    }

    private static String nvl(String s) { return s == null ? null : s.trim(); }
}
