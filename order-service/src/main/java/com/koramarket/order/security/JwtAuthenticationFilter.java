package com.koramarket.order.security;

import com.koramarket.common.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) { chain.doFilter(request,response); return; }

        var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) { chain.doFilter(request,response); return; }

        var jwt = header.substring(7);
        final String email;
        try {
            email = jwtUtil.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            response.setStatus(401); response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Access token expiré\"}");
            return;
        } catch (Exception e) {
            chain.doFilter(request,response); return;
        }

        if (!jwtUtil.validateToken(jwt)) { chain.doFilter(request,response); return; }

        var roles = toStringList(jwtUtil.extractClaim(jwt, c -> c.get("roles")));
        var perms = toStringList(jwtUtil.extractClaim(jwt, c -> c.get("perms")));

        var authorities = Stream.concat(
                roles.stream().filter(Objects::nonNull).map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r),
                perms.stream().filter(Objects::nonNull)
        ).map(SimpleGrantedAuthority::new).toList();

        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object raw) {
        if (raw == null) return List.of();
        if (raw instanceof List<?> l) {
            return l.stream().map(o -> {
                if (o == null) return null;
                if (o instanceof String s) return s;
                if (o instanceof Map<?, ?> m) {
                    Object authority = m.get("authority");
                    Object role = m.get("role");
                    Object v = authority != null ? authority : role;
                    return v != null ? v.toString() : null;
                }
                return o.toString();
            }).filter(Objects::nonNull).toList();
        }
        if (raw instanceof String s) {
            if (s.isBlank()) return List.of();
            return Arrays.stream(s.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
        }
        return List.of(raw.toString());
    }
}
