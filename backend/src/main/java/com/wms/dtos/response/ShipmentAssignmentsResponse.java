package com.wms.dtos.response;

public class ShipmentAssignmentsResponse {

    private final Long shipmentId;
    private final UserSummaryResponse picker;
    private final UserSummaryResponse packer;

    public ShipmentAssignmentsResponse(Long shipmentId, UserSummaryResponse picker, UserSummaryResponse packer) {
        this.shipmentId = shipmentId;
        this.picker = picker;
        this.packer = packer;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public UserSummaryResponse getPicker() {
        return picker;
    }

    public UserSummaryResponse getPacker() {
        return packer;
    }
}
