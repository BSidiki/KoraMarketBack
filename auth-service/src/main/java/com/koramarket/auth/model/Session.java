package com.koramarket.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true) // n’inclure que ce qu’on marque
@Table(
        name = "sessions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "token"),
                @UniqueConstraint(columnNames = "refresh_token")
        },
        indexes = {
                @Index(name = "idx_sessions_user_id", columnList = "user_id"),
                @Index(name = "idx_sessions_date_expiration", columnList = "date_expiration")
        }
)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 2048)
    @ToString.Exclude // token sensible
    private String token;

    @Column(name = "ip_adresse", length = 64)
    private String ipAdresse;

    @Builder.Default
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "navigateur", length = 512)
    private String navigateur;

    @Column(name = "refresh_token", nullable = false, unique = true, length = 512)
    @ToString.Exclude // secret aussi
    private String refreshToken;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}
