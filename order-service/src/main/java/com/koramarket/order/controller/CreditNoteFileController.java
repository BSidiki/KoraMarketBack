// CreditNoteFileController.java
package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.repository.CreditNoteRepository;
import com.koramarket.order.service.CreditNotePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/credit-notes")
@RequiredArgsConstructor
public class CreditNoteFileController {
    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("audit.creditnote");

    private final CreditNotePdfService pdfService;
    private final CreditNoteRepository creditNoteRepo;

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable UUID id,
                                         Authentication auth,
                                         @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                         @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPriv = hasAny(auth, "ADMIN","ORDER_READ_ANY","ORDER_EDIT_ANY");
        UUID userIdExt = userIdFromJwt != null ? userIdFromJwt : parseUuidOrNull(userIdHeader);

        var cn = creditNoteRepo.findById(id).orElseThrow(() -> new BusinessException("Avoir introuvable"));
        UUID owner = cn.getOrder().getUserIdExt();
        if (!isPriv && (userIdExt == null || !owner.equals(userIdExt))) {
            throw new BusinessException("Accès interdit à cet avoir");
        }

        AUDIT.info("CreditNote PDF viewed: id={} by user={}", id, (userIdExt != null ? userIdExt : "unk"));

        byte[] pdf = pdfService.renderById(id);
        HttpHeaders h = new HttpHeaders();
        h.setCacheControl("private, max-age=300");
        h.setContentType(MediaType.APPLICATION_PDF);
        h.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + cn.getCreditNumber() + ".pdf");
        return new ResponseEntity<>(pdf, h, HttpStatus.OK);
    }

    private static void ensureAuth(Authentication auth){ if (auth == null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise"); }
    private static boolean hasAny(Authentication auth, String... names){ var set=Set.of(names); return auth.getAuthorities().stream().anyMatch(a -> set.contains(a.getAuthority())); }
    private static UUID parseUuidOrNull(String raw){ if (raw == null || raw.isBlank()) return null; try { return UUID.fromString(raw.trim()); } catch (IllegalArgumentException e){ return null; } }
}
