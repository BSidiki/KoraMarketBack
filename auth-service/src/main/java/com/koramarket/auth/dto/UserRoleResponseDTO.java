package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class UserRoleResponseDTO {
    private Long id;
    private Long userId;
    private Long roleId;
}
