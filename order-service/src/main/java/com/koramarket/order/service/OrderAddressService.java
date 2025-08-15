package com.koramarket.order.service;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.OrderAddressRequestDTO;
import com.koramarket.order.dto.OrderAddressResponseDTO;
import com.koramarket.order.mapper.OrderAddressMapper;
import com.koramarket.order.model.Order;
import com.koramarket.order.model.OrderAddress;
import com.koramarket.order.repository.OrderAddressRepository;
import com.koramarket.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderAddressService {

    private final OrderRepository orderRepo;
    private final OrderAddressRepository addressRepo;

    @Value("${order.shipping.flat-rate:0}")          // ex: 2000 (centimes XOF)
    private long shippingFlatRate;

    @Value("${order.shipping.free-threshold:0}")     // ex: 10000000 (100k XOF)
    private long freeShippingThreshold;

    @Transactional
    public List<OrderAddressResponseDTO> list(UUID orderId, UUID userIdExt, boolean isAdminOrAny) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("Commande introuvable"));
        ensureOwnerOrAdmin(o, userIdExt, isAdminOrAny);
        return addressRepo.findByOrder_IdOrderByAddrTypeAsc(orderId).stream()
                .map(OrderAddressMapper::toResponse).toList();
    }

    @Transactional
    public OrderAddressResponseDTO upsert(UUID orderId, UUID userIdExt, boolean isAdminOrAny, OrderAddressRequestDTO req) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("Commande introuvable"));
        ensureOwnerOrAdmin(o, userIdExt, isAdminOrAny);
        ensureModifiable(o);

        OrderAddress a = addressRepo.findByOrder_IdAndAddrType(orderId, req.getAddrType())
                .orElseGet(() -> {
                    OrderAddress na = new OrderAddress();
                    na.setOrder(o);
                    return na;
                });

        OrderAddressMapper.apply(req, a);
        a = addressRepo.save(a);

        // Recalcul shipping (très simple pour démarrer)
        long shipping = computeShipping(o);
        o.setShippingTotalAmount(shipping);
        o.setGrandTotalAmount(o.getSubtotalAmount() + o.getTaxTotalAmount() + shipping - o.getDiscountTotalAmount());

        return OrderAddressMapper.toResponse(a);
    }

    public Optional<UUID> findIdByOrderNumber(String on) {
        return orderRepo.findByOrderNumber(on).map(Order::getId);
    }

    private long computeShipping(Order o) {
        long sub = o.getSubtotalAmount() == null ? 0L : o.getSubtotalAmount();
        if (freeShippingThreshold > 0 && sub >= freeShippingThreshold) return 0L;
        return Math.max(0L, shippingFlatRate);
    }

    private static void ensureOwnerOrAdmin(Order o, UUID userIdExt, boolean isAdminOrAny) {
        if (!isAdminOrAny && !o.getUserIdExt().equals(userIdExt)) {
            throw new BusinessException("Accès interdit à cette commande");
        }
    }

    private static void ensureModifiable(Order o) {
        // Tu peux étendre la règle selon ton workflow
        var allowed = List.of("PENDING", "AWAITING_PAYMENT");
        if (!allowed.contains(o.getOrderStatus())) {
            throw new BusinessException("Commande non modifiable à ce stade");
        }
    }
}
