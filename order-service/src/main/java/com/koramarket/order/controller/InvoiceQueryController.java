package com.koramarket.order.controller;

import com.koramarket.order.dto.InvoiceResponseDTO;
import com.koramarket.order.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceQueryController {

    private final InvoiceService invoiceService;

    @GetMapping("/by-number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponseDTO> getByNumber(@PathVariable String invoiceNumber,
                                                          Authentication auth) {
        // Security: déjà couverte par SecurityConfig (lecture)
        return invoiceService.findByNumber(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
