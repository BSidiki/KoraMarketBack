package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.RefundRequestDTO;
import com.koramarket.order.dto.RefundResponseDTO;
import com.koramarket.order.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponseDTO> create(@Valid @RequestBody RefundRequestDTO req,
                                                    Authentication auth) {
        ensureAuth(auth);
        boolean isPriv = hasAny(auth, "ADMIN","REFUND_CREATE");
        var dto = refundService.create(req, isPriv);
        return ResponseEntity.created(URI.create("/api/refunds/" + dto.getId())).body(dto);
    }

    @GetMapping("/{id}")
    public RefundResponseDTO getOne(@PathVariable UUID id, Authentication auth) {
        ensureAuth(auth);
        boolean isPriv = hasAny(auth, "ADMIN","REFUND_CREATE","ORDER_READ_ANY");
        if (!isPriv) throw new BusinessException("Permission refusée");
        return refundService.findOne(id);
    }

    private static void ensureAuth(Authentication auth){ if(auth==null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise"); }
    private static boolean hasAny(Authentication auth, String... names){ var set=Set.of(names); return auth.getAuthorities().stream().anyMatch(a->set.contains(a.getAuthority())); }
}
