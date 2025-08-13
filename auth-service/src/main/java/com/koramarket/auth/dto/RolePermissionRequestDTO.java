package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class RolePermissionRequestDTO {
    private Long roleId;
    private Long permissionId;
}
