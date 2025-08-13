package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class OauthClientResponseDTO {
    private Long id;
    private String clientId;
    private String scopes;
    private String redirectUri;
}
