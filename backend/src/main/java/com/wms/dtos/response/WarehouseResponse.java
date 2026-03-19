package com.wms.dtos.response;

public class WarehouseResponse {

    private final Long warehouseId;
    private final String location;
    private final Integer capacity;
    private final String email;
    private final Long managerId;
    private final String managerName;
    private final String managerEmail;

    public WarehouseResponse(
        Long warehouseId,
        String location,
        Integer capacity,
        String email,
        Long managerId,
        String managerName,
        String managerEmail
    ) {
        this.warehouseId = warehouseId;
        this.location = location;
        this.capacity = capacity;
        this.email = email;
        this.managerId = managerId;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getLocation() {
        return location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getEmail() {
        return email;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public String getManagerEmail() {
        return managerEmail;
    }
}
