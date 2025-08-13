package com.koramarket.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionResponseDTO {
    private Long id;
    private Long userId;
    private String token;
    private String ipAdresse;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private String navigateur;
}
