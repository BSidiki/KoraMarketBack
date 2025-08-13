package com.koramarket.auth.repository;

import com.koramarket.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        select distinct u
        from User u
        left join fetch u.userRoles ur
        left join fetch ur.role r
        where u.email = :email
    """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("""
        select distinct u
        from User u
        left join fetch u.userRoles ur
        left join fetch ur.role r
        left join fetch r.rolePermissions rp
        left join fetch rp.permission p
        where u.email = :email
    """)
    Optional<User> findByEmailWithRolesAndPerms(@Param("email") String email);
}
