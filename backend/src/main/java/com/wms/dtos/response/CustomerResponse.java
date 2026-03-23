package com.wms.dtos.response;

public class CustomerResponse {

    private final Long customerId;
    private final String name;
    private final String address;
    private final String phoneNum;
    private final String email;
    private final Long userId;

    public CustomerResponse(
        Long customerId,
        String name,
        String address,
        String phoneNum,
        String email,
        Long userId
    ) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.email = email;
        this.userId = userId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }
}
