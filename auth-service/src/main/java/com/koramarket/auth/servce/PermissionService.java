package com.koramarket.auth.servce;

import com.koramarket.auth.model.Permission;
import com.koramarket.auth.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    public Optional<Permission> findByNom(String nom) {
        return permissionRepository.findByNom(nom);
    }

    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }

    public void delete(Long id) {
        permissionRepository.deleteById(id);
    }
}
