package com.wms.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.response.AuditEventResponse;
import com.wms.models.AuditEvent;
import com.wms.models.User;
import com.wms.repositories.AuditEventRepository;
import com.wms.repositories.UserRepository;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;

    public AuditService(AuditEventRepository auditEventRepository, UserRepository userRepository) {
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logEvent(String eventType, String entityType, String entityId, String action, String details) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setAction(action);
        event.setDetails(details);
        event.setCreatedAt(LocalDateTime.now());
        event.setPerformedBy(resolveCurrentUser());
        auditEventRepository.save(event);
    }

    public List<AuditEventResponse> listRecentEvents() {
        return auditEventRepository.findTop200ByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    private User resolveCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                return null;
            }
            return userRepository.findByEmailIgnoreCase(authentication.getName()).orElse(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(
            event.getEventId(),
            event.getEventType(),
            event.getEntityType(),
            event.getEntityId(),
            event.getAction(),
            event.getPerformedBy() != null ? event.getPerformedBy().getUserId() : null,
            event.getPerformedBy() != null ? event.getPerformedBy().getName() : null,
            event.getDetails(),
            event.getCreatedAt()
        );
    }
}
