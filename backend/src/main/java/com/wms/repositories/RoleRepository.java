package com.wms.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);
}
