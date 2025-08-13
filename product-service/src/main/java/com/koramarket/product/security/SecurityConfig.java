package com.koramarket.product.security;

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
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(restAuthEntryPoint())
                        .accessDeniedHandler(restAccessDenied())
                )
                .authorizeHttpRequests(auth -> auth
                        // Préflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger (si activé)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Actuator
                        .requestMatchers(HttpMethod.GET, "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // GET spécifique AVANT règle générale
                        .requestMatchers(HttpMethod.GET, "/api/products/my-products")
                        .hasAnyAuthority("ROLE_ADMIN","ROLE_VENDEUR","PRODUCT_READ") // ou garde uniquement les rôles si tu préfères

                        // Public reads
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/product-images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/product-attributes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/product-reviews/**").permitAll()

                        // Produits
                        .requestMatchers(HttpMethod.POST, "/api/products/**")
                        .hasAnyAuthority("ROLE_ADMIN","ROLE_VENDEUR","PRODUCT_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                        .hasAnyAuthority("ROLE_ADMIN","ROLE_VENDEUR","PRODUCT_UPDATE_ANY","PRODUCT_UPDATE_OWN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**")
                        .hasAnyAuthority("ROLE_ADMIN","PRODUCT_DELETE_ANY","PRODUCT_DELETE_OWN")

                        // Images produit
                        .requestMatchers(HttpMethod.POST, "/api/product-images/**")
                        .hasAnyAuthority("ROLE_ADMIN","ROLE_VENDEUR","PRODUCT_IMAGE_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/product-images/**")
                        .hasAnyAuthority("ROLE_ADMIN","ROLE_VENDEUR","PRODUCT_IMAGE_UPDATE_ANY","PRODUCT_IMAGE_UPDATE_OWN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product-images/**")
                        .hasAnyAuthority("ROLE_ADMIN","PRODUCT_IMAGE_DELETE_ANY","PRODUCT_IMAGE_DELETE_OWN")

                        // Attributs produit
                        .requestMatchers(HttpMethod.POST, "/api/product-attributes/**")
                        .hasAnyAuthority("ROLE_ADMIN","ROLE_VENDEUR","PRODUCT_ATTRIBUTE_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/product-attributes/**")
                        .hasAnyAuthority("ROLE_ADMIN","ROLE_VENDEUR","PRODUCT_ATTRIBUTE_UPDATE_ANY","PRODUCT_ATTRIBUTE_UPDATE_OWN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product-attributes/**")
                        .hasAnyAuthority("ROLE_ADMIN","PRODUCT_ATTRIBUTE_DELETE_ANY","PRODUCT_ATTRIBUTE_DELETE_OWN")

                        // Catégories
                        .requestMatchers(HttpMethod.POST, "/api/categories/**")
                        .hasAnyAuthority("ROLE_ADMIN","CATEGORY_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**")
                        .hasAnyAuthority("ROLE_ADMIN","CATEGORY_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**")
                        .hasAnyAuthority("ROLE_ADMIN","CATEGORY_DELETE")

                        // Avis (client ou modération)
                        .requestMatchers(HttpMethod.POST, "/api/product-reviews/**")
                        .hasAnyAuthority("ROLE_CLIENT","ROLE_ADMIN","REVIEW_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/product-reviews/**")
                        .hasAnyAuthority("ROLE_CLIENT","ROLE_ADMIN","REVIEW_UPDATE_OWN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product-reviews/**")
                        .hasAnyAuthority("ROLE_CLIENT","ROLE_ADMIN","REVIEW_DELETE_OWN","REVIEW_MODERATE")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200","http://127.0.0.1:4200"));
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
    AuthenticationEntryPoint restAuthEntryPoint() {
        return (req, res, ex) -> {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\""+
                    (ex!=null?ex.getMessage():"")+"\"}");
        };
    }

    @Bean
    AccessDeniedHandler restAccessDenied() {
        return (req, res, ex) -> {
            res.setStatus(403);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\""+
                    (ex!=null?ex.getMessage():"")+"\"}");
        };
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
