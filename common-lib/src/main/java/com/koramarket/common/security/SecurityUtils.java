package com.koramarket.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.koramarket.common.dto.AuthUserInfo;
import com.koramarket.common.exceptions.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.List;

public class SecurityUtils {

    private SecurityUtils() {}

    /*public static AuthUserInfo getCurrentUser(HttpServletRequest request, JwtUtil jwtUtil) {
        String token = extractToken(request);
        if (token == null) throw new RuntimeException("Token JWT manquant !");
        Claims claims = jwtUtil.extractAllClaims(token); // Cette méthode doit être publique dans JwtUtil !
        String email = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);
        return new AuthUserInfo(email, roles);
    }*/

    private static String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Extraction du user (et rôles) depuis JWT dans le header Authorization
    /*public static AuthUserInfo getCurrentUser(HttpServletRequest request, JwtUtil jwtUtil) {
        String token = extractToken(request);
        if (token == null) throw new BusinessException("Token JWT manquant !");
        Claims claims = jwtUtil.extractAllClaims(token);
        String email = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);
        return new AuthUserInfo(email, roles);
    }*/

    private AuthUserInfo currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        String email = (String) auth.getPrincipal(); // tu as mis "username" = subject (email ?)
        List<String> roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList();

        return new AuthUserInfo(email, roles); // adapte à ton DTO
    }
}
