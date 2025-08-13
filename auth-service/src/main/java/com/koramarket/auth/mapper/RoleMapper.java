package com.koramarket.auth.mapper;

import com.koramarket.auth.model.Role;
import com.koramarket.auth.dto.RoleRequestDTO;
import com.koramarket.auth.dto.RoleResponseDTO;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
public class RoleMapper {

    public static Role toEntity(RoleRequestDTO dto) {
        Role role = new Role();
        role.setNom(dto.getNom());
        role.setDescription(dto.getDescription());
        return role;
    }

    public static RoleResponseDTO toResponse(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setNom(role.getNom());
        dto.setDescription(role.getDescription());
        if (role.getRolePermissions() != null) {
            dto.setPermissions(
                    role.getRolePermissions()
                            .stream()
                            .map(rp -> rp.getPermission().getNom())
                            .collect(Collectors.toSet())
            );
        }
        return dto;
    }
}
