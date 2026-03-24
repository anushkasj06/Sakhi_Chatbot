package com.wms.dtos.response;

import java.time.LocalDateTime;

public class HealthStatusResponse {

    private final String status;
    private final String service;
    private final LocalDateTime timestamp;
    private final String details;

    public HealthStatusResponse(String status, String service, LocalDateTime timestamp, String details) {
        this.status = status;
        this.service = service;
        this.timestamp = timestamp;
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public String getService() {
        return service;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }
}
