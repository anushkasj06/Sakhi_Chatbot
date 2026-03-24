package com.wms.dtos.response;

import java.time.LocalDateTime;

public class InventoryMovementResponse {

    private final Long movementId;
    private final LocalDateTime createdAt;
    private final Long warehouseId;
    private final String warehouseLocation;
    private final Long productId;
    private final String productName;
    private final Integer quantityDelta;
    private final String movementType;
    private final String reason;
    private final String referenceType;
    private final String referenceId;

    public InventoryMovementResponse(
        Long movementId,
        LocalDateTime createdAt,
        Long warehouseId,
        String warehouseLocation,
        Long productId,
        String productName,
        Integer quantityDelta,
        String movementType,
        String reason,
        String referenceType,
        String referenceId
    ) {
        this.movementId = movementId;
        this.createdAt = createdAt;
        this.warehouseId = warehouseId;
        this.warehouseLocation = warehouseLocation;
        this.productId = productId;
        this.productName = productName;
        this.quantityDelta = quantityDelta;
        this.movementType = movementType;
        this.reason = reason;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    public Long getMovementId() {
        return movementId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public String getMovementType() {
        return movementType;
    }

    public String getReason() {
        return reason;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }
}
