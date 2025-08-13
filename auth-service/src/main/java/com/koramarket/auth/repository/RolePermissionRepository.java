package com.koramarket.auth.repository;

import com.koramarket.auth.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
}
