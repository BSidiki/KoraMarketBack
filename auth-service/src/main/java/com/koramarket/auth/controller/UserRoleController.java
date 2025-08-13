package com.koramarket.auth.controller;

import com.koramarket.auth.servce.UserRoleService;
import com.koramarket.auth.dto.UserRoleRequestDTO;
import com.koramarket.auth.dto.UserRoleResponseDTO;
import com.koramarket.auth.mapper.UserRoleMapper;
import com.koramarket.auth.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping
    public List<UserRoleResponseDTO> getAllUserRoles() {
        return userRoleService.findAll().stream()
                .map(UserRoleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRoleResponseDTO> getUserRoleById(@PathVariable Long id) {
        return userRoleService.findById(id)
                .map(UserRoleMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserRoleResponseDTO> createUserRole(@RequestBody UserRoleRequestDTO dto) {
        UserRole saved = userRoleService.createUserRole(dto);
        return ResponseEntity.ok(UserRoleMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserRole(@PathVariable Long id) {
        if (userRoleService.findById(id).isPresent()) {
            userRoleService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
