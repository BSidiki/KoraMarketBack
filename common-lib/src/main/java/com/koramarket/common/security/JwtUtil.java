package com.koramarket.common.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${jwt.prefix:Bearer }")
    private String tokenPrefix;

    @Value("${jwt.header:Authorization}")
    private String headerString;

    // Générer un token simple
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    // Générer un token avec claims personnalisés (ex: rôles)
    public String generateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    // Extraire tous les claims (utilisable UNIQUEMENT via l’instance !)
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    // Extraire un claim particulier
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraire username (= subject du token)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Validation générale du token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }

    // Getters si besoin pour les tests
    public String getTokenPrefix() { return tokenPrefix; }
    public String getHeaderString() { return headerString; }
}
