package com.koramarket.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "order_addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderAddress {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "addr_type", length = 16, nullable = false)
    private AddressType addrType; // SHIPPING / BILLING

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "line1")
    private String line1;

    @Column(name = "line2")
    private String line2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country", length = 2)
    private String country;

    @PrePersist
    void prePersist() { if (id == null) id = UUID.randomUUID(); }
}
