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
import java.util.Set;
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
        ensureAuth(auth);
        // GET: READ_ANY ou EDIT_ANY ou ROLE_ADMIN peuvent consulter n’importe quelle commande
        boolean isPrivileged = hasAnyAuthority(auth, "ROLE_ADMIN", "ORDER_READ_ANY", "ORDER_EDIT_ANY");

        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isPrivileged) throw new BusinessException("Identifiant utilisateur manquant");

        return addressService.list(orderId, userIdExt, isPrivileged);
    }

    @PostMapping
    public ResponseEntity<OrderAddressResponseDTO> upsert(@PathVariable UUID orderId,
                                                          @Valid @RequestBody OrderAddressRequestDTO req,
                                                          Authentication auth,
                                                          @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                          @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        // POST: seuls EDIT_ANY ou ROLE_ADMIN sont “privilégiés”
        boolean isPrivileged = hasAnyAuthority(auth, "ROLE_ADMIN", "ORDER_EDIT_ANY");

        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isPrivileged) throw new BusinessException("Identifiant utilisateur manquant");

        var dto = addressService.upsert(orderId, userIdExt, isPrivileged, req);
        return ResponseEntity.created(URI.create("/api/orders/" + orderId + "/addresses/" + dto.getId())).body(dto);
    }

    // ---- Helpers ----
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
