package com.koramarket.auth.controller;

import com.koramarket.auth.servce.PermissionService;
import com.koramarket.auth.dto.PermissionRequestDTO;
import com.koramarket.auth.dto.PermissionResponseDTO;
import com.koramarket.auth.mapper.PermissionMapper;
import com.koramarket.auth.model.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public List<PermissionResponseDTO> getAllPermissions() {
        return permissionService.findAll().stream()
                .map(PermissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> getPermissionById(@PathVariable Long id) {
        return permissionService.findById(id)
                .map(PermissionMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PermissionResponseDTO> createPermission(@RequestBody PermissionRequestDTO dto) {
        Permission permission = PermissionMapper.toEntity(dto);
        Permission saved = permissionService.save(permission);
        return ResponseEntity.ok(PermissionMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> updatePermission(@PathVariable Long id, @RequestBody PermissionRequestDTO dto) {
        return permissionService.findById(id)
                .map(existing -> {
                    existing.setNom(dto.getNom());
                    existing.setDescription(dto.getDescription());
                    Permission updated = permissionService.save(existing);
                    return ResponseEntity.ok(PermissionMapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable Long id) {
        if (permissionService.findById(id).isPresent()) {
            permissionService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
