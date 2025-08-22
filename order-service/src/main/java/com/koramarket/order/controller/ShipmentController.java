// controller/ShipmentController.java
package com.koramarket.order.controller;

import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.order.dto.*;
import com.koramarket.order.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponseDTO> create(@Valid @RequestBody ShipmentRequestDTO req,
                                                      Authentication auth,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                      @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPriv = hasAny(auth, "ADMIN","ORDER_FULFILL","VENDOR");
        UUID userIdExt = userIdFromJwt!=null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        var dto = shipmentService.create(req, userIdExt, isPriv);
        return ResponseEntity.created(URI.create("/api/shipments/" + dto.getId())).body(dto);
    }

    @PostMapping("/{id}/events")
    public ShipmentResponseDTO event(@PathVariable UUID id,
                                     @Valid @RequestBody ShipmentStatusEventDTO evt,
                                     Authentication auth,
                                     @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                     @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean isPriv = hasAny(auth, "ADMIN","ORDER_FULFILL","VENDOR");
        UUID userIdExt = userIdFromJwt!=null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        return shipmentService.event(id, evt, userIdExt, isPriv);
    }

    @GetMapping("/by-order/{orderId}")
    public List<ShipmentResponseDTO> listByOrder(@PathVariable UUID orderId,
                                                 Authentication auth,
                                                 @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                 @RequestAttribute(value = "userIdExt", required = false) UUID userIdFromJwt) {
        ensureAuth(auth);
        boolean canReadAny = hasAny(auth, "ADMIN","ORDER_READ_ANY","ORDER_FULFILL");
        UUID userIdExt = userIdFromJwt!=null ? userIdFromJwt : parseUuidOrNull(userIdHeader);
        return shipmentService.listByOrder(orderId, userIdExt, canReadAny);
    }

    private static void ensureAuth(Authentication auth){ if(auth==null || !auth.isAuthenticated()) throw new BusinessException("Authentification requise"); }
    private static boolean hasAny(Authentication auth, String... names){ var set=Set.of(names); return auth.getAuthorities().stream().anyMatch(a -> set.contains(a.getAuthority())); }
    private static UUID parseUuidOrNull(String raw){ if(raw==null || raw.isBlank()) return null; try{ return UUID.fromString(raw.trim()); } catch(IllegalArgumentException e){ return null; } }
}
