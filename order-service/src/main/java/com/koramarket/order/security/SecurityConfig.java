package com.koramarket.order.security;

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

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .exceptionHandling(e -> e.authenticationEntryPoint(restAuthEntryPoint()).accessDeniedHandler(restAccessDenied()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // ---- Invoices ----
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/*/invoice")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_READ_OWN","ORDER_READ_ANY")

                        // Invoices JSON
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/invoices/by-number/*")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_READ_OWN","ORDER_READ_ANY")
                        // Invoices PDF
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/invoices/*/pdf", "/api/invoices/by-number/*/pdf")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_READ_OWN","ORDER_READ_ANY")

                        // ---- orders ----
                        .requestMatchers(HttpMethod.POST, "/api/orders/**")
                        .hasAnyAuthority("CLIENT","ORDER_CREATE")
                        .requestMatchers(HttpMethod.GET, "/api/orders/my/**")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_READ_OWN","ORDER_READ_ANY")
                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                        .hasAnyAuthority("ADMIN","ORDER_READ_ANY")
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**")
                        .hasAnyAuthority("ADMIN","ORDER_CANCEL_ANY","ORDER_CANCEL_OWN")

                        // Payments
                        // 1) Capture (règle spécifique AVANT la règle générique)
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/capture")
                        .hasAnyAuthority("SUPER_ADMIN", "PAYMENT_CAPTURE")

                        // 2) Création d'intent
                        .requestMatchers(HttpMethod.POST, "/api/payments")
                        .hasAnyAuthority("CLIENT", "PAYMENT_INIT")

                        // 3) Éventuels autres POST /api/payments/** (si tu en ajoutes)
                        .requestMatchers(HttpMethod.POST, "/api/payments/**")
                        .hasAnyAuthority("CLIENT", "PAYMENT_INIT")

                        // Refunds (souvent staff/admin)
                        .requestMatchers(HttpMethod.POST, "/api/refunds/**")
                        .hasAnyAuthority("ADMIN","REFUND_CREATE")

                        .requestMatchers(HttpMethod.GET, "/api/orders/*/addresses", "/api/orders/*/addresses/**")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_READ_OWN","ORDER_READ_ANY")

                        // Upsert (create/update) addresses for an order
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/addresses", "/api/orders/*/addresses/**")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_EDIT_OWN","ORDER_EDIT_ANY")

                        // Upsert (create/update) addresses for an order by number
                        .requestMatchers(HttpMethod.GET,  "/api/orders/by-number/*/addresses/**")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_READ_OWN","ORDER_READ_ANY")
                        .requestMatchers(HttpMethod.POST, "/api/orders/by-number/*/addresses/**")
                        .hasAnyAuthority("CLIENT","ADMIN","ORDER_EDIT_OWN","ORDER_EDIT_ANY")

                        // Shipments
                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasAnyAuthority("ADMIN","ORDER_FULFILL","VENDEUR")
                        .requestMatchers(HttpMethod.POST, "/api/shipments/*/events").hasAnyAuthority("ADMIN","ORDER_FULFILL","VENDEUR")
                        .requestMatchers(HttpMethod.GET,  "/api/shipments/by-order/**").hasAnyAuthority("ADMIN","ORDER_READ_ANY","ORDER_FULFILL","CLIENT")

                        // Refunds
                        .requestMatchers(HttpMethod.POST, "/api/refunds/**").hasAnyAuthority("ADMIN","REFUND_CREATE")
                        .requestMatchers(HttpMethod.GET,  "/api/refunds/**").hasAnyAuthority("ADMIN","REFUND_CREATE","ORDER_READ_ANY")

                        // Credit Notes
                        .requestMatchers(HttpMethod.GET, "/api/credit-notes/**")
                        .hasAnyAuthority("ADMIN","ORDER_READ_ANY","ORDER_EDIT_ANY","CLIENT")


                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200","http://127.0.0.1:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean AuthenticationEntryPoint restAuthEntryPoint() {
        return (req,res,ex) -> {
            res.setStatus(401); res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\""+(ex!=null?ex.getMessage():"")+"\"}");
        };
    }

    @Bean AccessDeniedHandler restAccessDenied() {
        return (req,res,ex) -> {
            res.setStatus(403); res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\""+(ex!=null?ex.getMessage():"")+"\"}");
        };
    }

    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
