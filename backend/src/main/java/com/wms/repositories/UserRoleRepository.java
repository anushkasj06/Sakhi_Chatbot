package com.wms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.User;
import com.wms.models.UserRole;
import com.wms.models.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUser(User user);

    void deleteByUserUserIdAndRoleRoleId(Long userId, Integer roleId);

    boolean existsByUserUserIdAndRoleRoleId(Long userId, Integer roleId);
}
