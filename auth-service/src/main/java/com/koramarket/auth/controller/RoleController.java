package com.koramarket.auth.controller;

import com.koramarket.auth.servce.RoleService;
import com.koramarket.auth.dto.RoleRequestDTO;
import com.koramarket.auth.dto.RoleResponseDTO;
import com.koramarket.auth.mapper.RoleMapper;
import com.koramarket.auth.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponseDTO> getAllRoles() {
        return roleService.findAll().stream()
                .map(RoleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(RoleMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(@RequestBody RoleRequestDTO dto) {
        Role role = RoleMapper.toEntity(dto);
        Role saved = roleService.save(role);
        return ResponseEntity.ok(RoleMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> updateRole(@PathVariable Long id, @RequestBody RoleRequestDTO dto) {
        return roleService.findById(id)
                .map(existing -> {
                    existing.setNom(dto.getNom());
                    existing.setDescription(dto.getDescription());
                    Role updated = roleService.save(existing);
                    return ResponseEntity.ok(RoleMapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        if (roleService.findById(id).isPresent()) {
            roleService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
