package com.wms.dtos.response;

public class UserSummaryResponse {

    private final Long userId;
    private final String name;
    private final String email;

    public UserSummaryResponse(Long userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
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
}
