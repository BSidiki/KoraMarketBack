package com.koramarket.auth.controller;

import com.koramarket.auth.servce.AuditLogService;
import com.koramarket.auth.dto.AuditLogResponseDTO;
import com.koramarket.auth.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLogResponseDTO> getAllAuditLogs() {
        return auditLogService.findAll().stream()
                .map(AuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponseDTO> getAuditLogById(@PathVariable Long id) {
        return auditLogService.findById(id)
                .map(AuditLogMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
