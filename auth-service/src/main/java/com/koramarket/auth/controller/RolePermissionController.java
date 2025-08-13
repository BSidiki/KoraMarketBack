package com.koramarket.auth.controller;

import com.koramarket.auth.servce.RolePermissionService;
import com.koramarket.auth.dto.RolePermissionRequestDTO;
import com.koramarket.auth.dto.RolePermissionResponseDTO;
import com.koramarket.auth.mapper.RolePermissionMapper;
import com.koramarket.auth.model.RolePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping
    public List<RolePermissionResponseDTO> getAllRolePermissions() {
        return rolePermissionService.findAll().stream()
                .map(RolePermissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolePermissionResponseDTO> getRolePermissionById(@PathVariable Long id) {
        return rolePermissionService.findById(id)
                .map(RolePermissionMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RolePermissionResponseDTO> createRolePermission(@RequestBody RolePermissionRequestDTO dto) {
        RolePermission saved = rolePermissionService.createRolePermission(dto);
        return ResponseEntity.ok(RolePermissionMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRolePermission(@PathVariable Long id) {
        if (rolePermissionService.findById(id).isPresent()) {
            rolePermissionService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
