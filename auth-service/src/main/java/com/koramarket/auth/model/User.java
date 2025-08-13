package com.koramarket.auth.model;/*
package com.koramarket.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Setter @NoArgsConstructor
@AllArgsConstructor @Builder
@ToString(exclude = {"userRoles","sessions","auditLogs"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    private String telephone;

    private LocalDateTime dateInscription = LocalDateTime.now();

    private String statut;

    private LocalDateTime lastLogin;

    // --- Relations ---

    // Association avec les rôles (user_roles)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    // Sessions actives de l'utilisateur
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Session> sessions = new HashSet<>();

    // Audit logs de l'utilisateur
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AuditLog> auditLogs = new HashSet<>();

    public void addRole(Role role) {
        UserRole ur = new UserRole();
        ur.setUser(this);
        ur.setRole(role);
        this.userRoles.add(ur);
        role.getUserRoles().add(ur);
    }
    public void removeRole(Role role) {
        this.userRoles.removeIf(ur -> ur.getRole().equals(role) && ur.getUser().equals(this));
        role.getUserRoles().removeIf(ur -> ur.getUser().equals(this));
    }
}
*/


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)  // 👈 n’inclure que ce qu’on marque
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include private String email;
    private String nom;
    private String prenom;
    private String motDePasse;
    private String telephone;
    private LocalDateTime dateInscription = LocalDateTime.now();
    private String statut;
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // 👈 JAMAIS dans toString
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Session> sessions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<AuditLog> auditLogs = new HashSet<>();
}
