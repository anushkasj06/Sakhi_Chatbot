package com.wms.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.response.LowStockAlertResponse;
import com.wms.enums.RoleName;
import com.wms.exceptions.ApiException;
import com.wms.models.Inventory;
import com.wms.models.LowStockAlert;
import com.wms.models.Role;
import com.wms.models.User;
import com.wms.models.UserRole;
import com.wms.repositories.InventoryRepository;
import com.wms.repositories.LowStockAlertRepository;
import com.wms.repositories.RoleRepository;
import com.wms.repositories.UserRepository;
import com.wms.repositories.UserRoleRepository;

@Service
public class LowStockAlertService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_ACKNOWLEDGED = "ACKNOWLEDGED";
    private static final String STATUS_RESOLVED = "RESOLVED";

    private final LowStockAlertRepository lowStockAlertRepository;
    private final InventoryRepository inventoryRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.alerts.low-stock.threshold:10}")
    private Integer lowStockThreshold;

    public LowStockAlertService(
        LowStockAlertRepository lowStockAlertRepository,
        InventoryRepository inventoryRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository,
        UserRepository userRepository,
        EmailService emailService
    ) {
        this.lowStockAlertRepository = lowStockAlertRepository;
        this.inventoryRepository = inventoryRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public List<LowStockAlertResponse> listAlerts(String status, boolean mineOnly) {
        List<LowStockAlert> alerts;
        if (status != null && !status.isBlank()) {
            alerts = lowStockAlertRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        } else {
            alerts = lowStockAlertRepository.findByStatusInOrderByCreatedAtDesc(Arrays.asList(STATUS_OPEN, STATUS_ACKNOWLEDGED, STATUS_RESOLVED));
        }

        if (mineOnly) {
            Long currentUserId = getCurrentUser().getUserId();
            alerts = alerts.stream()
                .filter(a -> a.getAssignedUser() != null && a.getAssignedUser().getUserId().equals(currentUserId))
                .toList();
        }

        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional
    public LowStockAlertResponse acknowledgeAlert(Long alertId) {
        LowStockAlert alert = findAlert(alertId);
        if (STATUS_RESOLVED.equals(alert.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Resolved alert cannot be acknowledged");
        }

        User user = getCurrentUser();
        alert.setStatus(STATUS_ACKNOWLEDGED);
        alert.setAcknowledgedBy(user);
        alert.setAcknowledgedAt(LocalDateTime.now());
        return toResponse(lowStockAlertRepository.save(alert));
    }

    @Transactional
    public LowStockAlertResponse resolveAlert(Long alertId) {
        LowStockAlert alert = findAlert(alertId);
        if (STATUS_RESOLVED.equals(alert.getStatus())) {
            return toResponse(alert);
        }

        alert.setStatus(STATUS_RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        return toResponse(lowStockAlertRepository.save(alert));
    }

    @Transactional
    @Scheduled(fixedDelayString = "${app.alerts.low-stock.scan-ms:300000}")
    public void scanAndGenerateAlerts() {
        runLowStockScan();
    }

    @Transactional
    public Integer runLowStockScan() {
        int generatedCount = 0;
        List<Inventory> lowStocks = inventoryRepository.findByQuantityLessThanEqual(lowStockThreshold);

        for (Inventory inventory : lowStocks) {
            LowStockAlert alert = lowStockAlertRepository
                .findFirstByInventoryInventoryIdAndStatusInOrderByCreatedAtDesc(
                    inventory.getInventoryId(),
                    Arrays.asList(STATUS_OPEN, STATUS_ACKNOWLEDGED)
                )
                .orElse(null);

            if (alert == null) {
                alert = new LowStockAlert();
                alert.setInventory(inventory);
                alert.setStatus(STATUS_OPEN);
                alert.setCreatedAt(LocalDateTime.now());
                alert.setNotificationCount(0);
                generatedCount++;
            }

            alert.setThreshold(lowStockThreshold);
            alert.setCurrentQuantity(inventory.getQuantity());
            alert.setAssignedUser(resolveRecipient(inventory));
            alert.setUpdatedAt(LocalDateTime.now());

            LowStockAlert saved = lowStockAlertRepository.save(alert);
            sendNotification(saved);
        }

        List<LowStockAlert> activeAlerts = lowStockAlertRepository.findByStatusInOrderByCreatedAtDesc(Arrays.asList(STATUS_OPEN, STATUS_ACKNOWLEDGED));
        for (LowStockAlert active : activeAlerts) {
            if (active.getInventory().getQuantity() > lowStockThreshold) {
                active.setStatus(STATUS_RESOLVED);
                active.setResolvedAt(LocalDateTime.now());
                active.setCurrentQuantity(active.getInventory().getQuantity());
                active.setUpdatedAt(LocalDateTime.now());
                lowStockAlertRepository.save(active);
            }
        }

        return generatedCount;
    }

    private void sendNotification(LowStockAlert alert) {
        User recipient = alert.getAssignedUser();
        if (recipient == null || recipient.getEmail() == null) {
            return;
        }

        emailService.sendLowStockAlertEmail(
            recipient.getEmail(),
            alert.getInventory().getProduct().getName(),
            alert.getInventory().getWarehouse().getLocation(),
            alert.getCurrentQuantity(),
            alert.getThreshold()
        );

        alert.setNotificationCount(alert.getNotificationCount() + 1);
        alert.setLastNotifiedAt(LocalDateTime.now());
        lowStockAlertRepository.save(alert);
    }

    private User resolveRecipient(Inventory inventory) {
        User manager = inventory.getWarehouse().getManager();
        if (manager != null && Boolean.TRUE.equals(manager.getIsActive())) {
            return manager;
        }

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ADMIN role not found"));

        return userRoleRepository.findByRoleRoleId(adminRole.getRoleId()).stream()
            .map(UserRole::getUser)
            .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
            .findFirst()
            .orElse(null);
    }

    private LowStockAlert findAlert(Long alertId) {
        return lowStockAlertRepository.findById(alertId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Low stock alert not found"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private LowStockAlertResponse toResponse(LowStockAlert alert) {
        return new LowStockAlertResponse(
            alert.getAlertId(),
            alert.getInventory().getInventoryId(),
            alert.getInventory().getProduct().getProductId(),
            alert.getInventory().getProduct().getName(),
            alert.getInventory().getWarehouse().getWarehouseId(),
            alert.getInventory().getWarehouse().getLocation(),
            alert.getThreshold(),
            alert.getCurrentQuantity(),
            alert.getStatus(),
            alert.getAssignedUser() != null ? alert.getAssignedUser().getUserId() : null,
            alert.getAssignedUser() != null ? alert.getAssignedUser().getName() : null,
            alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getUserId() : null,
            alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getName() : null,
            alert.getAcknowledgedAt(),
            alert.getResolvedAt(),
            alert.getNotificationCount(),
            alert.getLastNotifiedAt(),
            alert.getCreatedAt(),
            alert.getUpdatedAt()
        );
    }
}
