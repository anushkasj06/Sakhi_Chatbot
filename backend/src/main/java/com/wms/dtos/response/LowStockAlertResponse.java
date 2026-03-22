package com.wms.dtos.response;

import java.time.LocalDateTime;

public class LowStockAlertResponse {

    private final Long alertId;
    private final Long inventoryId;
    private final Long productId;
    private final String productName;
    private final Long warehouseId;
    private final String warehouseLocation;
    private final Integer threshold;
    private final Integer currentQuantity;
    private final String status;
    private final Long assignedUserId;
    private final String assignedUserName;
    private final Long acknowledgedByUserId;
    private final String acknowledgedByUserName;
    private final LocalDateTime acknowledgedAt;
    private final LocalDateTime resolvedAt;
    private final Integer notificationCount;
    private final LocalDateTime lastNotifiedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public LowStockAlertResponse(
        Long alertId,
        Long inventoryId,
        Long productId,
        String productName,
        Long warehouseId,
        String warehouseLocation,
        Integer threshold,
        Integer currentQuantity,
        String status,
        Long assignedUserId,
        String assignedUserName,
        Long acknowledgedByUserId,
        String acknowledgedByUserName,
        LocalDateTime acknowledgedAt,
        LocalDateTime resolvedAt,
        Integer notificationCount,
        LocalDateTime lastNotifiedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.alertId = alertId;
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.productName = productName;
        this.warehouseId = warehouseId;
        this.warehouseLocation = warehouseLocation;
        this.threshold = threshold;
        this.currentQuantity = currentQuantity;
        this.status = status;
        this.assignedUserId = assignedUserId;
        this.assignedUserName = assignedUserName;
        this.acknowledgedByUserId = acknowledgedByUserId;
        this.acknowledgedByUserName = acknowledgedByUserName;
        this.acknowledgedAt = acknowledgedAt;
        this.resolvedAt = resolvedAt;
        this.notificationCount = notificationCount;
        this.lastNotifiedAt = lastNotifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getAlertId() {
        return alertId;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public String getStatus() {
        return status;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public String getAssignedUserName() {
        return assignedUserName;
    }

    public Long getAcknowledgedByUserId() {
        return acknowledgedByUserId;
    }

    public String getAcknowledgedByUserName() {
        return acknowledgedByUserName;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public Integer getNotificationCount() {
        return notificationCount;
    }

    public LocalDateTime getLastNotifiedAt() {
        return lastNotifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
