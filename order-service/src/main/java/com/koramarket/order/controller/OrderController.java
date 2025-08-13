package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.OrderRequestDTO;
import com.koramarket.order.dto.OrderResponseDTO;
import com.koramarket.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ---- Create order --------------------------------------------------------
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO req,
                                                   Authentication auth,
                                                   @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                                   @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,      // fallback Postman
                                                   @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {   // posé par filtre JWT
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Authentification requise");
        }

        // 1) Priorité au userId extrait du JWT par le filtre
        UUID userIdExt = userIdFromJwt;

        // 2) Fallback temporaire: header X-User-Id (UUID)
        if (userIdExt == null) {
            userIdExt = parseUuidOrNull(userIdHeader);
        }

        if (userIdExt == null) {
            // Message aligné avec le service
            throw new BusinessException("Identifiant utilisateur manquant");
        }

        OrderResponseDTO dto = orderService.create(req, userIdExt, idemKey);
        return ResponseEntity.created(URI.create("/api/orders/" + dto.getId())).body(dto);
    }

    // ---- My orders -----------------------------------------------------------
    @GetMapping("/my")
    public List<OrderResponseDTO> my(Authentication auth,
                                     @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                     @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Authentification requise");
        }
        UUID userIdExt = (userIdFromJwt != null) ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null) {
            throw new BusinessException("Identifiant utilisateur manquant");
        }
        return orderService.myOrders(userIdExt);
    }

    // ---- Get one -------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOne(@PathVariable UUID id) {
        return orderService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---- Cancel --------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id,
                                       Authentication auth,
                                       @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                       @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Authentification requise");
        }
        boolean isAdminOrAny = auth.getAuthorities().stream().anyMatch(ga ->
                "ROLE_ADMIN".equals(ga.getAuthority()) || "ORDER_CANCEL_ANY".equals(ga.getAuthority()));

        UUID userIdExt = (userIdFromJwt != null) ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isAdminOrAny) {
            // si admin/ORDER_CANCEL_ANY, on peut autoriser sans owner
            throw new BusinessException("Identifiant utilisateur manquant");
        }
        orderService.cancelOwn(id, userIdExt, isAdminOrAny);
        return ResponseEntity.ok().build();
    }

    // ---- Helpers -------------------------------------------------------------
    private static UUID parseUuidOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return UUID.fromString(raw.trim()); } catch (IllegalArgumentException e) { return null; }
    }
}
