package com.wms.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PurchaseOrderResponse {

    private final Long poId;
    private final Long supplierId;
    private final String supplierName;
    private final Long warehouseId;
    private final String warehouseLocation;
    private final LocalDateTime orderDate;
    private final LocalDateTime expectedDeliveryDate;
    private final BigDecimal totalAmount;
    private final String status;
    private final Long receiverId;
    private final String receiverName;
    private final List<PurchaseOrderItemResponse> items;

    public PurchaseOrderResponse(
        Long poId,
        Long supplierId,
        String supplierName,
        Long warehouseId,
        String warehouseLocation,
        LocalDateTime orderDate,
        LocalDateTime expectedDeliveryDate,
        BigDecimal totalAmount,
        String status,
        Long receiverId,
        String receiverName,
        List<PurchaseOrderItemResponse> items
    ) {
        this.poId = poId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.warehouseId = warehouseId;
        this.warehouseLocation = warehouseLocation;
        this.orderDate = orderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.items = items;
    }

    public Long getPoId() {
        return poId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public List<PurchaseOrderItemResponse> getItems() {
        return items;
    }
}
