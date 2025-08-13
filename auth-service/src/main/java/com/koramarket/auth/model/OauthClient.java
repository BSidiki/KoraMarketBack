package com.koramarket.auth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true) // exclude secret
@Table(
        name = "oauth_clients",
        uniqueConstraints = @UniqueConstraint(columnNames = "client_id"),
        indexes = @Index(name = "idx_oauth_client_id", columnList = "client_id")
)
public class OauthClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true, length = 128)
    @ToString.Include
    private String clientId;

    @Column(name = "client_secret", nullable = false, length = 512)
    @ToString.Exclude // secret: jamais dans toString
    private String clientSecret;

    @Column(name = "scopes", length = 512)
    private String scopes; // ex: "read,write,products:*"

    @Column(name = "redirect_uri", length = 1024)
    private String redirectUri;
}
