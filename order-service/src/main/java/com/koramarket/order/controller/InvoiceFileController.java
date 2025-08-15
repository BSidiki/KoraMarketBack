// InvoiceFileController.java
package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.repository.InvoiceRepository;
import com.koramarket.order.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceFileController {

    private final InvoicePdfService pdfService;
    private final InvoiceRepository invoiceRepo;

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable UUID id,
                                         Authentication auth,
                                         @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                         @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPrivileged = hasAnyAuthority(auth, "ADMIN", "ORDER_READ_ANY", "ORDER_EDIT_ANY");
        UUID userIdExt = (userIdFromJwt != null) ? userIdFromJwt : parseUuidOrNull(userIdHeader);

        var inv = invoiceRepo.findById(id).orElseThrow(() -> new BusinessException("Facture introuvable"));
        var orderOwner = inv.getOrder().getUserIdExt();

        if (!isPrivileged && (userIdExt == null || !orderOwner.equals(userIdExt))) {
            throw new BusinessException("Accès interdit à cette facture");
        }

        byte[] pdf = pdfService.renderByInvoiceId(id);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + inv.getInvoiceNumber() + ".pdf");
        return new ResponseEntity<>(pdf, h, HttpStatus.OK);
    }

    // (dans InvoiceFileController existant)
    @GetMapping("/by-number/{invoiceNumber}/pdf")
    public ResponseEntity<byte[]> getPdfByNumber(@PathVariable String invoiceNumber,
                                                 Authentication auth,
                                                 @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                 @RequestAttribute(value = "userIdExt", required = false) java.util.UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPrivileged = hasAnyAuthority(auth, "ADMIN", "ORDER_READ_ANY", "ORDER_EDIT_ANY");
        var inv = invoiceRepo.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new com.koramarket.common.exceptions.BusinessException("Facture introuvable"));
        var owner = inv.getOrder().getUserIdExt();
        java.util.UUID userIdExt = (userIdFromJwt != null) ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        if (!isPrivileged && (userIdExt == null || !owner.equals(userIdExt))) {
            throw new com.koramarket.common.exceptions.BusinessException("Accès interdit à cette facture");
        }
        byte[] pdf = pdfService.renderByInvoiceId(inv.getId());
        org.springframework.http.HttpHeaders h = new org.springframework.http.HttpHeaders();
        h.set(org.springframework.http.HttpHeaders.CACHE_CONTROL, "private, max-age=300");
        h.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        h.set(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + inv.getInvoiceNumber() + ".pdf");
        return new org.springframework.http.ResponseEntity<>(pdf, h, org.springframework.http.HttpStatus.OK);
    }


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
