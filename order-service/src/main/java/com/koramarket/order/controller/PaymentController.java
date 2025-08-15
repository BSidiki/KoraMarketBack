package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.PaymentRequestDTO;
import com.koramarket.order.dto.PaymentResponseDTO;
import com.koramarket.order.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createIntent(@Valid @RequestBody PaymentRequestDTO req,
                                                           Authentication auth,
                                                           @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                                           @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                           @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPrivileged = hasAnyAuthority(auth, "CLIENT", "ADMIN", "PAYMENT_INIT", "ORDER_READ_ANY", "ORDER_EDIT_ANY");

        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (userIdExt == null && !isPrivileged) throw new BusinessException("Identifiant utilisateur manquant");

        var dto = paymentService.createIntent(req, idemKey, userIdExt, isPrivileged);
        return ResponseEntity.created(URI.create("/api/payments/" + dto.getId())).body(dto);
    }

    @PostMapping("/{id}/capture")
    public ResponseEntity<PaymentResponseDTO> capture(@PathVariable UUID id,
                                                      Authentication auth) {
        ensureAuth(auth);
        boolean isPrivileged = hasAnyAuthority(auth, "SUPER_ADMIN", "PAYMENT_CAPTURE");
        var dto = paymentService.capture(id, isPrivileged);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/webhooks/{provider}")
    public ResponseEntity<Void> providerWebhook(@PathVariable String provider,
                                                @RequestBody Map<String,Object> payload) {
        // 1) vérifier signature
        // 2) trouver le paiement via externalTransactionId
        // 3) passer en AUTHORIZED/CAPTURED selon l’événement
        return ResponseEntity.ok().build();
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
