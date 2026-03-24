package com.wms.dtos.response;

public class InventorySummaryResponse {

    private final Long warehouseId;
    private final String warehouseLocation;
    private final Integer distinctProducts;
    private final Integer totalQuantity;
    private final Integer lowStockItems;

    public InventorySummaryResponse(
        Long warehouseId,
        String warehouseLocation,
        Integer distinctProducts,
        Integer totalQuantity,
        Integer lowStockItems
    ) {
        this.warehouseId = warehouseId;
        this.warehouseLocation = warehouseLocation;
        this.distinctProducts = distinctProducts;
        this.totalQuantity = totalQuantity;
        this.lowStockItems = lowStockItems;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public Integer getDistinctProducts() {
        return distinctProducts;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public Integer getLowStockItems() {
        return lowStockItems;
    }
}
