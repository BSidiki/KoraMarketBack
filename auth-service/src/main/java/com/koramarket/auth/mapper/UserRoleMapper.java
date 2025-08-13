package com.koramarket.auth.mapper;

import com.koramarket.auth.dto.UserRoleResponseDTO;
import com.koramarket.auth.model.UserRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserRoleMapper {

    public static UserRoleResponseDTO toResponse(UserRole userRole) {
        UserRoleResponseDTO dto = new UserRoleResponseDTO();
        dto.setId(userRole.getId());
        dto.setUserId(userRole.getUser() != null ? userRole.getUser().getId() : null);
        dto.setRoleId(userRole.getRole() != null ? userRole.getRole().getId() : null);
        return dto;
    }
}
