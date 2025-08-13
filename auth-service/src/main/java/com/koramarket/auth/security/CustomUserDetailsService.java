package com.koramarket.auth.security;

import com.koramarket.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // garde la session ouverte pendant la construction des autorités
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmailWithRolesAndPerms(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));

        // LinkedHashSet pour éviter les doublons et garder un ordre stable
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();

        // Rôles -> "ROLE_*"
        user.getUserRoles().forEach(ur -> {
            var role = ur.getRole();
            if (role != null && role.getNom() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getNom()));
            }
        });

        // Permissions -> telles quelles (ex: "USER_ROLE_READ")
        user.getUserRoles().forEach(ur -> {
            var role = ur.getRole();
            if (role != null && role.getRolePermissions() != null) {
                role.getRolePermissions().forEach(rp -> {
                    if (rp.getPermission() != null && rp.getPermission().getNom() != null) {
                        authorities.add(new SimpleGrantedAuthority(rp.getPermission().getNom()));
                    }
                });
            }
        });

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getMotDePasse(),
                authorities
        );
    }
}
