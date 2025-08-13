package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class UserRoleRequestDTO {
    private Long userId;
    private Long roleId;
}
