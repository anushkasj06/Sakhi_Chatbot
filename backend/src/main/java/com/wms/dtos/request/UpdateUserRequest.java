package com.wms.dtos.request;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(max = 255)
    private String name;

    @Size(min = 8, max = 100)
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
