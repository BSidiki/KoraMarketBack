package com.koramarket.auth.model;/*
package com.koramarket.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id","permission_id"}))
public class RolePermission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

}
*/


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id","permission_id"}))
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id", nullable = false)
    @ToString.Exclude
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "permission_id", nullable = false)
    @ToString.Exclude
    private Permission permission;
}
