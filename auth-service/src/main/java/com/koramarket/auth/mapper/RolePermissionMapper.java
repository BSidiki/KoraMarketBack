package com.koramarket.auth.mapper;

import com.koramarket.auth.dto.RolePermissionResponseDTO;
import com.koramarket.auth.model.Permission;
import com.koramarket.auth.model.Role;
import com.koramarket.auth.dto.RolePermissionRequestDTO;
import com.koramarket.auth.model.RolePermission;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RolePermissionMapper {

    public static RolePermissionResponseDTO toResponse(RolePermission rp) {
        RolePermissionResponseDTO dto = new RolePermissionResponseDTO();
        dto.setId(rp.getId());
        dto.setRoleId(rp.getRole() != null ? rp.getRole().getId() : null);
        dto.setPermissionId(rp.getPermission() != null ? rp.getPermission().getId() : null);
        return dto;
    }


    public static RolePermission toEntity(RolePermissionRequestDTO dto) {
        RolePermission rp = new RolePermission();
        rp.setRole(new Role());
        rp.setPermission(new Permission());
        return rp;
    }
}
