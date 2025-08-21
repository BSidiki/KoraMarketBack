package com.koramarket.product.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.product.dto.ReleaseStockRequestDTO;
import com.koramarket.product.dto.ReserveStockRequestDTO;
import com.koramarket.product.dto.ReserveStockResponseDTO;
import com.koramarket.product.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/products/stock")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public ReserveStockResponseDTO reserve(@Valid @RequestBody ReserveStockRequestDTO req, Authentication auth) {
        ensureAuth(auth);
        ensurePriv(auth); // à ajuster selon ta security
        return inventoryService.reserve(req);
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@Valid @RequestBody ReleaseStockRequestDTO req, Authentication auth) {
        ensureAuth(auth);
        ensurePriv(auth);
        inventoryService.release(req);
        return ResponseEntity.ok().build();
    }

    private static void ensureAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise");
    }
    private static void ensurePriv(Authentication auth) {
        var ok = auth.getAuthorities().stream().anyMatch(a ->
                Set.of("ADMIN","ORDER_SERVICE","ORDER_CREATE").contains(a.getAuthority()));
        if (!ok) throw new BusinessException("Accès refusé");
    }
}

