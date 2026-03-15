package com.wms.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreateRoleRequest;
import com.wms.exceptions.ApiException;
import com.wms.models.Role;
import com.wms.repositories.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public Role createRole(CreateRoleRequest request) {
        String roleName = request.getRoleName().trim().toUpperCase();
        if (roleRepository.existsByRoleName(roleName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Role already exists");
        }

        Role role = new Role();
        role.setRoleName(roleName);
        return roleRepository.save(role);
    }
}
