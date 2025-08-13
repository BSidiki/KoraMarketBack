package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class RolePermissionResponseDTO {
    private Long id;
    private Long roleId;
    private Long permissionId;
}
