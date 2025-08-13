package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.OrderAddressRequestDTO;
import com.koramarket.order.dto.OrderAddressResponseDTO;
import com.koramarket.order.service.OrderAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/addresses")
@RequiredArgsConstructor
public class OrderAddressController {

    private final OrderAddressService addressService;

    @GetMapping
    public List<OrderAddressResponseDTO> list(@PathVariable UUID orderId,
                                              Authentication auth,
                                              @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                              @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        if (auth == null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise");
        boolean isAdminOrAny = auth.getAuthorities().stream().anyMatch(ga ->
                "ADMIN".equals(ga.getAuthority()) || "ORDER_EDIT_ANY".equals(ga.getAuthority()));

        UUID userIdExt = (userIdFromJwt != null) ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isAdminOrAny) throw new BusinessException("Identifiant utilisateur manquant");

        return addressService.list(orderId, userIdExt, isAdminOrAny);
    }

    @PostMapping
    public ResponseEntity<OrderAddressResponseDTO> upsert(@PathVariable UUID orderId,
                                                          @Valid @RequestBody OrderAddressRequestDTO req,
                                                          Authentication auth,
                                                          @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                          @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        if (auth == null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise");
        boolean isAdminOrAny = auth.getAuthorities().stream().anyMatch(ga ->
                "ADMIN".equals(ga.getAuthority()) || "ORDER_EDIT_ANY".equals(ga.getAuthority()));

        UUID userIdExt = (userIdFromJwt != null) ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isAdminOrAny) throw new BusinessException("Identifiant utilisateur manquant");

        var dto = addressService.upsert(orderId, userIdExt, isAdminOrAny, req);
        // 201 si création ; 200 si update — pour simplifier on renvoie 201
        return ResponseEntity.created(URI.create("/api/orders/" + orderId + "/addresses/" + dto.getId()))
                .body(dto);
    }

    private static UUID parseUuidOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return UUID.fromString(raw.trim()); } catch (IllegalArgumentException e) { return null; }
    }
}
