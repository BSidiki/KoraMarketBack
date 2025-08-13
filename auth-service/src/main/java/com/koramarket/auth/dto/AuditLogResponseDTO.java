package com.koramarket.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogResponseDTO {
    private Long id;
    private Long userId;
    private String action;
    private String endpoint;
    private LocalDateTime date;
    private String ipAdresse;
    private String details;
}
