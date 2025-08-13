package com.koramarket.order.repository;

import com.koramarket.order.model.AddressType;
import com.koramarket.order.model.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, UUID> {
    List<OrderAddress> findByOrder_IdOrderByAddrTypeAsc(UUID orderId);
    Optional<OrderAddress> findByOrder_IdAndAddrType(UUID orderId, AddressType addrType);
}
