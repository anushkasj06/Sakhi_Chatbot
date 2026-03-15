package com.wms.dtos.request;

import jakarta.validation.constraints.NotBlank;

public class AssignRoleRequest {

    @NotBlank
    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
