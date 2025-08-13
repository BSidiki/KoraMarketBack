package com.koramarket.auth.servce; // attention : corrige "servce" en "service" dans le nom du package

import com.koramarket.auth.dto.RolePermissionRequestDTO;
import com.koramarket.auth.model.Permission;
import com.koramarket.auth.model.Role;
import com.koramarket.auth.model.RolePermission;
import com.koramarket.auth.repository.PermissionRepository;
import com.koramarket.auth.repository.RolePermissionRepository;
import com.koramarket.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermission createRolePermission(RolePermissionRequestDTO dto) {
        // 1. Charger le vrai Role depuis la BDD
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id " + dto.getRoleId()));

        // 2. Charger la vraie Permission depuis la BDD
        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id " + dto.getPermissionId()));

        // 3. Créer la liaison
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);

        // 4. Sauver
        return rolePermissionRepository.save(rolePermission);
    }

    public List<RolePermission> findAll() {
        return rolePermissionRepository.findAll();
    }

    public Optional<RolePermission> findById(Long id) {
        return rolePermissionRepository.findById(id);
    }

    public RolePermission save(RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    public void delete(Long id) {
        rolePermissionRepository.deleteById(id);
    }
}
