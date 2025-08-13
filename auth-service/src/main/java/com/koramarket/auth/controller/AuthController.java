package com.koramarket.auth.controller;

import com.koramarket.auth.audit.AuditedAction;
import com.koramarket.auth.dto.UserRequestDTO;
import com.koramarket.auth.dto.UserResponseDTO;
import com.koramarket.auth.mapper.UserMapper;
import com.koramarket.auth.model.Session;
import com.koramarket.auth.model.User;
import com.koramarket.auth.servce.SessionService;
import com.koramarket.auth.servce.UserService;
import com.koramarket.common.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    @AuditedAction("REGISTER")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO dto) {
        if (userService.findByEmailWithRoles(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User user = UserMapper.toEntity(dto);
        user.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        User saved = userService.save(user);
        return ResponseEntity.ok(UserMapper.toResponse(saved));
    }

    @Data
    public static class AuthResponseDTO {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";

        public AuthResponseDTO(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDTO dto) {
        // ⚠️ important: on charge aussi les permissions pour les inclure dans le JWT
        User user = userService.findByEmailWithRolesAndPerms(dto.getEmail())
                .filter(u -> passwordEncoder.matches(dto.getMotDePasse(), u.getMotDePasse()))
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("Identifiants invalides");
        }

        // Rôles + Permissions pour le token
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getNom())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> perms = user.getUserRoles().stream()
                .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getNom())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        claims.put("roles", roles);
        claims.put("perms", perms);

        String accessToken = jwtUtil.generateToken(claims, user.getEmail());
        String refreshToken = UUID.randomUUID().toString();

        // Enregistre la session (refresh token en BDD)
        Session session = new Session();
        session.setUser(user);
        session.setToken(accessToken); // optionnel
        session.setRefreshToken(refreshToken);
        session.setDateCreation(LocalDateTime.now());
        session.setDateExpiration(LocalDateTime.now().plusDays(7));
        sessionService.save(session);

        System.out.println("[LOGIN] " + user.getEmail()
                + " | roles=" + roles
                + " | perms=" + perms);

        return ResponseEntity.ok(new AuthResponseDTO(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token manquant");
        }

        Session session = sessionService.findByRefreshToken(refreshToken)
                .filter(s -> s.getDateExpiration().isAfter(LocalDateTime.now()))
                .orElse(null);

        if (session == null) {
            return ResponseEntity.status(401).body("Refresh token invalide ou expiré");
        }

        // ⚠️ Recharger l'utilisateur avec rôles + permissions (pas l'entité attachée à Session)
        String email = session.getUser().getEmail();
        User user = userService.findByEmailWithRolesAndPerms(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + email));

        Map<String, Object> claims = new HashMap<>();
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getNom())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> perms = user.getUserRoles().stream()
                .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getNom())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        claims.put("roles", roles);
        claims.put("perms", perms);

        String accessToken = jwtUtil.generateToken(claims, email);

        return ResponseEntity.ok(new AuthResponseDTO(accessToken, refreshToken));
    }

    @AuditedAction("PROFILE_USER")
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> profile(Principal principal) {
        String email = principal.getName();
        var opt = userService.findByEmailWithRoles(email);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        User u = opt.get();
        System.out.println("[/profile] " + email + " | rolesSize=" + (u.getUserRoles()!=null ? u.getUserRoles().size() : null));
        u.getUserRoles().forEach(ur -> System.out.println("   - role=" + ur.getRole().getNom()));

        return ResponseEntity.ok(UserMapper.toResponse(u));
    }

//     (Optionnel) Petit endpoint de debug pour voir ce que Spring “voit” comme authorities
     @GetMapping("/_whoami")
     public Map<String,Object> whoami() {
         var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
         return Map.of(
             "principal", a!=null? a.getPrincipal(): null,
             "authorities", a!=null? a.getAuthorities().stream()
                 .map(org.springframework.security.core.GrantedAuthority::getAuthority).toList() : List.of()
         );
     }
}
