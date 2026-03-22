package com.wms.dtos.response;

import java.time.LocalDateTime;

public class ShipmentResponse {

    private final Long shipmentId;
    private final Long orderId;
    private final Long warehouseId;
    private final String warehouseLocation;
    private final LocalDateTime shipmentDate;
    private final String trackingNumber;
    private final String status;
    private final Long pickerId;
    private final String pickerName;
    private final Long packerId;
    private final String packerName;

    public ShipmentResponse(
        Long shipmentId,
        Long orderId,
        Long warehouseId,
        String warehouseLocation,
        LocalDateTime shipmentDate,
        String trackingNumber,
        String status,
        Long pickerId,
        String pickerName,
        Long packerId,
        String packerName
    ) {
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.warehouseId = warehouseId;
        this.warehouseLocation = warehouseLocation;
        this.shipmentDate = shipmentDate;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.pickerId = pickerId;
        this.pickerName = pickerName;
        this.packerId = packerId;
        this.packerName = packerName;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public LocalDateTime getShipmentDate() {
        return shipmentDate;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getStatus() {
        return status;
    }

    public Long getPickerId() {
        return pickerId;
    }

    public String getPickerName() {
        return pickerName;
    }

    public Long getPackerId() {
        return packerId;
    }

    public String getPackerName() {
        return packerName;
    }
}
