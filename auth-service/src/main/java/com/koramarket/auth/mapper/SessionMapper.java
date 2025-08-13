package com.koramarket.auth.mapper;

import com.koramarket.auth.dto.SessionResponseDTO;
import com.koramarket.auth.model.Session;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SessionMapper {

    public static SessionResponseDTO toResponse(Session session) {
        SessionResponseDTO dto = new SessionResponseDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUser() != null ? session.getUser().getId() : null);
        dto.setToken(session.getToken());
        dto.setIpAdresse(session.getIpAdresse());
        dto.setDateCreation(session.getDateCreation());
        dto.setDateExpiration(session.getDateExpiration());
        dto.setNavigateur(session.getNavigateur());
        return dto;
    }
}
