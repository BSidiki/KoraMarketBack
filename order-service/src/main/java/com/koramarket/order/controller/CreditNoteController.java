// CreditNoteController.java
package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.CreditNoteResponseDTO;
import com.koramarket.order.repository.CreditNoteRepository;
import com.koramarket.order.service.CreditNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/credit-notes")
@RequiredArgsConstructor
public class CreditNoteController {

    private final CreditNoteService creditNoteService;
    private final CreditNoteRepository creditNoteRepository;

    @GetMapping("/{id}")
    public ResponseEntity<CreditNoteResponseDTO> getOne(@PathVariable UUID id,
                                                        Authentication auth,
                                                        @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                        @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPriv = hasAny(auth, "ADMIN","ORDER_READ_ANY","ORDER_EDIT_ANY");

        var cn = creditNoteRepository.findById(id).orElse(null);
        if (cn == null) return ResponseEntity.notFound().build();

        // Ownership: le client ne peut lire que ses avoirs
        UUID owner = cn.getOrder().getUserIdExt();
        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (!isPriv && (userIdExt == null || !owner.equals(userIdExt))) {
            throw new BusinessException("Accès interdit à cet avoir");
        }

        return creditNoteService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-number/{number}")
    public ResponseEntity<CreditNoteResponseDTO> getByNumber(@PathVariable String number,
                                                             Authentication auth,
                                                             @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                             @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPriv = hasAny(auth, "ADMIN","ORDER_READ_ANY","ORDER_EDIT_ANY");

        var opt = creditNoteService.findByNumber(number);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        var cn = opt.get();
        UUID owner = cn.getOrderId(); // on a seulement l’id dans le DTO
        // Revalider ownership via repo si besoin :
        var cnEntity = creditNoteRepository.findByCreditNumber(number).orElseThrow();
        UUID realOwner = cnEntity.getOrder().getUserIdExt();

        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (!isPriv && (userIdExt == null || !realOwner.equals(userIdExt))) {
            throw new BusinessException("Accès interdit à cet avoir");
        }
        return ResponseEntity.ok(cn);
    }

    private static void ensureAuth(Authentication auth){ if (auth == null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise"); }
    private static boolean hasAny(Authentication auth, String... names){ var set=Set.of(names); return auth.getAuthorities().stream().anyMatch(a -> set.contains(a.getAuthority())); }
    private static UUID parseUuidOrNull(String raw){ if (raw == null || raw.isBlank()) return null; try { return UUID.fromString(raw.trim()); } catch (IllegalArgumentException e){ return null; } }
}
