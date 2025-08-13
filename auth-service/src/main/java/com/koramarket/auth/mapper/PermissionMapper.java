package com.koramarket.auth.mapper;

import com.koramarket.auth.dto.PermissionResponseDTO;
import com.koramarket.auth.model.Permission;
import com.koramarket.auth.dto.PermissionRequestDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PermissionMapper {

    public static Permission toEntity(PermissionRequestDTO dto) {
        Permission permission = new Permission();
        if (dto.getId() != null) permission.setId(dto.getId());
        permission.setNom(dto.getNom());
        permission.setDescription(dto.getDescription());
        return permission;
    }


    public static PermissionResponseDTO toResponse(Permission permission) {
        PermissionResponseDTO dto = new PermissionResponseDTO();
        dto.setId(permission.getId());
        dto.setNom(permission.getNom());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}
