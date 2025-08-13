package com.koramarket.common.security;

public interface JwtConstants {
    String SECRET_KEY_PROP = "${jwt.secret}";
    String EXPIRATION_PROP = "${jwt.expiration:86400000}";
    String TOKEN_PREFIX_PROP = "${jwt.prefix:Bearer }";
    String HEADER_PROP = "${jwt.header:Authorization}";
}
