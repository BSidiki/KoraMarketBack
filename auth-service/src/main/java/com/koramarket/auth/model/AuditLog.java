package com.koramarket.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user_id", columnList = "user_id"),
                @Index(name = "idx_audit_date", columnList = "date")
        }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "action", length = 128, nullable = false)
    private String action;

    @Column(name = "endpoint", length = 512)
    private String endpoint;

    @Builder.Default
    @Column(name = "date", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "ip_adresse", length = 64)
    private String ipAdresse;

    @Lob
    @Column(name = "details")
    private String details;
}
