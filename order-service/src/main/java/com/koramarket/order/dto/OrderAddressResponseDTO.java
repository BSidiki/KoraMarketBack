package com.koramarket.order.dto;

import com.koramarket.order.model.AddressType;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderAddressResponseDTO {
    private UUID id;
    private AddressType addrType;
    private String fullName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
