package com.koramarket.auth.mapper;

import com.koramarket.auth.dto.OauthClientRequestDTO;
import com.koramarket.auth.dto.OauthClientResponseDTO;
import com.koramarket.auth.model.OauthClient;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OauthClientMapper {

    public static OauthClient toEntity(OauthClientRequestDTO dto) {
        OauthClient client = new OauthClient();
        client.setClientId(dto.getClientId());
        client.setClientSecret(dto.getClientSecret());
        client.setScopes(dto.getScopes());
        client.setRedirectUri(dto.getRedirectUri());
        return client;
    }

    public static OauthClientResponseDTO toResponse(OauthClient client) {
        OauthClientResponseDTO dto = new OauthClientResponseDTO();
        dto.setId(client.getId());
        dto.setClientId(client.getClientId());
        dto.setScopes(client.getScopes());
        dto.setRedirectUri(client.getRedirectUri());
        return dto;
    }
}
