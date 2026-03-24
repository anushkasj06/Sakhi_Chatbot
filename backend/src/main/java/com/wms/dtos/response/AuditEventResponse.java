package com.wms.dtos.response;

import java.time.LocalDateTime;

public class AuditEventResponse {

    private final Long eventId;
    private final String eventType;
    private final String entityType;
    private final String entityId;
    private final String action;
    private final Long performedByUserId;
    private final String performedByUserName;
    private final String details;
    private final LocalDateTime createdAt;

    public AuditEventResponse(
        Long eventId,
        String eventType,
        String entityType,
        String entityId,
        String action,
        Long performedByUserId,
        String performedByUserName,
        String details,
        LocalDateTime createdAt
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.performedByUserId = performedByUserId;
        this.performedByUserName = performedByUserName;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public Long getPerformedByUserId() {
        return performedByUserId;
    }

    public String getPerformedByUserName() {
        return performedByUserName;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
