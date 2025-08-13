package com.koramarket.order.dto;

import com.koramarket.order.model.AddressType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderAddressRequestDTO {
    @NotNull
    private AddressType addrType; // SHIPPING | BILLING

    private String fullName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country; // "BF", "ML", etc.
}
