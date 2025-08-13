package com.koramarket.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API stateless + pas de CSRF pour JWT
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CORS (pré-requêtes OPTIONS)
                .cors(c -> c.configurationSource(corsConfigurationSource()))

                // Erreurs JSON claires
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(restAuthEntryPoint())
                        .accessDeniedHandler(restAccessDenied())
                )

                // Règles d’accès
                .authorizeHttpRequests(auth -> auth
                        // Préflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger (dev)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Actuator: n’ouvre que health/info
                        .requestMatchers(HttpMethod.GET, "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Auth publics
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                        // Profil : doit être authentifié (ne pas laisser en public)
                        .requestMatchers("/api/auth/profile").authenticated()

                        // ----- Gestion des RÔLES -----
                        .requestMatchers(HttpMethod.GET,    "/api/roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_READ")
                        .requestMatchers(HttpMethod.POST,   "/api/roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_CREATE")
                        .requestMatchers(HttpMethod.PUT,    "/api/roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_DELETE")

                        // ----- Gestion des PERMISSIONS -----
                        .requestMatchers(HttpMethod.GET,    "/api/permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "PERMISSION_READ")
                        .requestMatchers(HttpMethod.POST,   "/api/permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "PERMISSION_CREATE")
                        .requestMatchers(HttpMethod.PUT,    "/api/permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "PERMISSION_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "PERMISSION_DELETE")

                        // ----- Attribution RÔLE ↔ UTILISATEUR -----
                        .requestMatchers(HttpMethod.GET,    "/api/user-roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "USER_ROLE_READ")
                        .requestMatchers(HttpMethod.POST,   "/api/user-roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "USER_ROLE_ASSIGN")
                        .requestMatchers(HttpMethod.PUT,    "/api/user-roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "USER_ROLE_ASSIGN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user-roles/**")
                        .hasAnyAuthority("ROLE_ADMIN", "USER_ROLE_REVOKE")

                        // ----- Attribution PERMISSION ↔ RÔLE -----
                        .requestMatchers(HttpMethod.GET,    "/api/role-permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PERMISSION_READ")
                        .requestMatchers(HttpMethod.POST,   "/api/role-permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PERMISSION_ASSIGN")
                        .requestMatchers(HttpMethod.PUT,    "/api/role-permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PERMISSION_ASSIGN")
                        .requestMatchers(HttpMethod.DELETE, "/api/role-permissions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PERMISSION_REVOKE")

                        // Users & OAuth clients : admin only
                        .requestMatchers("/api/users/**", "/api/oauth-clients/**").hasRole("ADMIN")

                        // Le reste
                        .anyRequest().authenticated()
                )

                // Filtre JWT avant UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS pour ton front (adapte les origins si nécessaire)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint restAuthEntryPoint() {
        return (req, res, ex) -> {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\""+
                    (ex!=null?ex.getMessage():"")+"\"}");
        };
    }

    @Bean
    public AccessDeniedHandler restAccessDenied() {
        return (req, res, ex) -> {
            res.setStatus(403);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\""+
                    (ex!=null?ex.getMessage():"")+"\"}");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
