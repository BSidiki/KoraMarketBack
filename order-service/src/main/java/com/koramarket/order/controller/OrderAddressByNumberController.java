package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.OrderAddressRequestDTO;
import com.koramarket.order.dto.OrderAddressResponseDTO;
import com.koramarket.order.service.OrderAddressService;
import com.koramarket.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/by-number/{orderNumber}/addresses")
@RequiredArgsConstructor
public class OrderAddressByNumberController {

    private final OrderService orderService;
    private final OrderAddressService addressService;

    @GetMapping
    public List<OrderAddressResponseDTO> listByNumber(@PathVariable String orderNumber,
                                                      Authentication auth,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                      @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPrivileged = hasAnyAuthority(auth, "ROLE_ADMIN", "ORDER_READ_ANY", "ORDER_EDIT_ANY");

        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isPrivileged) throw new BusinessException("Identifiant utilisateur manquant");

        UUID orderId = orderService.findIdByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException("Commande introuvable"));
        return addressService.list(orderId, userIdExt, isPrivileged);
    }

    @PostMapping
    public ResponseEntity<OrderAddressResponseDTO> upsertByNumber(@PathVariable String orderNumber,
                                                                  @Valid @RequestBody OrderAddressRequestDTO req,
                                                                  Authentication auth,
                                                                  @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                                  @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPrivileged = hasAnyAuthority(auth, "ROLE_ADMIN", "ORDER_EDIT_ANY");

        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isPrivileged) throw new BusinessException("Identifiant utilisateur manquant");

        UUID orderId = orderService.findIdByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException("Commande introuvable"));

        var dto = addressService.upsert(orderId, userIdExt, isPrivileged, req);
        return ResponseEntity.created(URI.create("/api/orders/" + orderId + "/addresses/" + dto.getId())).body(dto);
    }

    // ---- Helpers (mêmes que ci-dessus) ----
    private static void ensureAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise");
    }
    private static boolean hasAnyAuthority(Authentication auth, String... names) {
        var set = Set.of(names);
        return auth.getAuthorities().stream().anyMatch(ga -> set.contains(ga.getAuthority()));
    }
    private static UUID parseUuidOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return UUID.fromString(raw.trim()); } catch (IllegalArgumentException e) { return null; }
    }
}
