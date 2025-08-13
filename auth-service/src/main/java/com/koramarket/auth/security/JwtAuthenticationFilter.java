package com.koramarket.auth.security;

import com.koramarket.common.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Si déjà authentifié, on laisse la main
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String jwt = header.substring(7);

        String email;
        try {
            email = jwtUtil.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Access token expiré\"}");
            return;
        } catch (Exception e) {
            // Token invalide: on laisse passer sans auth (les endpoints publics pourront répondre)
            chain.doFilter(request, response);
            return;
        }

        // Valide la signature/expiration
        if (!jwtUtil.validateToken(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        // Récupère roles + perms du token (tolérant: List<String> / String / List<Map>)
        List<String> roles = toStringList(jwtUtil.extractClaim(jwt, claims -> claims.get("roles")));
        List<String> perms = toStringList(jwtUtil.extractClaim(jwt, claims -> claims.get("perms")));

        // Map en authorities Spring : rôles => ROLE_*, permissions => tel quel
        List<GrantedAuthority> authorities = Stream.concat(
                        roles.stream()
                                .filter(Objects::nonNull)
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r),
                        perms.stream().filter(Objects::nonNull)
                )
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // On met l'email (subject) comme principal
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object raw) {
        if (raw == null) return List.of();

        if (raw instanceof List<?> list) {
            return list.stream().map(o -> {
                if (o == null) return null;
                if (o instanceof String s) return s;
                if (o instanceof Map<?,?> m) {
                    Object v = m.containsKey("authority") ? m.get("authority") : m.get("role");
                    return v != null ? v.toString() : null;
                }
                return o.toString();
            }).filter(Objects::nonNull).toList();
        }

        if (raw instanceof String s) {
            if (s.isBlank()) return List.of();
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .toList();
        }

        // fallback: un seul élément non-list
        return List.of(raw.toString());
    }
}
