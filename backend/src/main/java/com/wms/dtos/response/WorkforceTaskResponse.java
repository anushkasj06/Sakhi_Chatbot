package com.wms.dtos.response;

import java.time.LocalDateTime;

public class WorkforceTaskResponse {

    private final String taskType;
    private final Long referenceId;
    private final String referenceNumber;
    private final String status;
    private final Long warehouseId;
    private final String warehouseLocation;
    private final LocalDateTime taskDate;

    public WorkforceTaskResponse(
        String taskType,
        Long referenceId,
        String referenceNumber,
        String status,
        Long warehouseId,
        String warehouseLocation,
        LocalDateTime taskDate
    ) {
        this.taskType = taskType;
        this.referenceId = referenceId;
        this.referenceNumber = referenceNumber;
        this.status = status;
        this.warehouseId = warehouseId;
        this.warehouseLocation = warehouseLocation;
        this.taskDate = taskDate;
    }

    public String getTaskType() {
        return taskType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getStatus() {
        return status;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public LocalDateTime getTaskDate() {
        return taskDate;
    }
}
