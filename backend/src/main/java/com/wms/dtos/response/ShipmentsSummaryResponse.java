package com.wms.dtos.response;

import java.util.Map;

public class ShipmentsSummaryResponse {

    private final Integer totalShipments;
    private final Map<String, Long> statusCounts;

    public ShipmentsSummaryResponse(Integer totalShipments, Map<String, Long> statusCounts) {
        this.totalShipments = totalShipments;
        this.statusCounts = statusCounts;
    }

    public Integer getTotalShipments() {
        return totalShipments;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }
}
