package com.wms.dtos.response;

import java.util.List;

public class UserResponse {

    private final Long userId;
    private final String name;
    private final String email;
    private final boolean active;
    private final boolean emailVerified;
    private final List<String> roles;

    public UserResponse(Long userId, String name, String email, boolean active, boolean emailVerified, List<String> roles) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.active = active;
        this.emailVerified = emailVerified;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public List<String> getRoles() {
        return roles;
    }
}
