package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.InvoiceResponseDTO;
import com.koramarket.order.model.Order;
import com.koramarket.order.repository.OrderRepository;
import com.koramarket.order.service.InvoiceService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final OrderRepository orderRepo;

    @GetMapping
    public ResponseEntity<InvoiceResponseDTO> getByOrder(@PathVariable @NotNull UUID orderId,
                                                         Authentication auth,
                                                         @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                         @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPrivileged = hasAnyAuthority(auth, "ADMIN", "ORDER_READ_ANY", "ORDER_EDIT_ANY");

        UUID userIdExt = (userIdFromJwt != null) ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isPrivileged) throw new BusinessException("Identifiant utilisateur manquant");

        // contrôle d'accès par ownership (si pas privilégié)
        if (!isPrivileged) {
            Order o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("Commande introuvable"));
            if (!o.getUserIdExt().equals(userIdExt)) throw new BusinessException("Accès interdit à cette commande");
        }

        return invoiceService.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // helpers
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
