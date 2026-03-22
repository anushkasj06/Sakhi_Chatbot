package com.wms.dtos.response;

public class InventoryResponse {

    private final Long inventoryId;
    private final Long productId;
    private final String productName;
    private final Long warehouseId;
    private final String warehouseLocation;
    private final Integer quantity;
    private final String locationInWarehouse;

    public InventoryResponse(
        Long inventoryId,
        Long productId,
        String productName,
        Long warehouseId,
        String warehouseLocation,
        Integer quantity,
        String locationInWarehouse
    ) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.productName = productName;
        this.warehouseId = warehouseId;
        this.warehouseLocation = warehouseLocation;
        this.quantity = quantity;
        this.locationInWarehouse = locationInWarehouse;
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

    public Integer getQuantity() {
        return quantity;
    }

    public String getLocationInWarehouse() {
        return locationInWarehouse;
    }
}
