package com.koramarket.auth.mapper;

import com.koramarket.auth.dto.AuditLogResponseDTO;
import com.koramarket.auth.model.AuditLog;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditLogMapper {

    public static AuditLogResponseDTO toResponse(AuditLog log) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setId(log.getId());
        dto.setUserId(log.getUser() != null ? log.getUser().getId() : null);
        dto.setAction(log.getAction());
        dto.setEndpoint(log.getEndpoint());
        dto.setDate(log.getDate());
        dto.setIpAdresse(log.getIpAdresse());
        dto.setDetails(log.getDetails());
        return dto;
    }
}
