package com.wms.dtos.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class CreatePurchaseOrderRequest {

    @NotNull
    private Long supplierId;

    @NotNull
    private Long warehouseId;

    private LocalDateTime expectedDeliveryDate;

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
}
