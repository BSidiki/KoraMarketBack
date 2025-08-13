package com.koramarket.auth.dto;

import lombok.Data;

@Data
public class OauthClientRequestDTO {
    private String clientId;
    private String clientSecret;
    private String scopes;
    private String redirectUri;
}
