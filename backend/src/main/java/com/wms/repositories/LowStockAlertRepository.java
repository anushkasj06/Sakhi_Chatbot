package com.wms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.LowStockAlert;

public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, Long> {

    List<LowStockAlert> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    List<LowStockAlert> findByStatusOrderByCreatedAtDesc(String status);

    List<LowStockAlert> findByStatusInAndAssignedUserUserIdOrderByCreatedAtDesc(List<String> statuses, Long userId);

    Optional<LowStockAlert> findFirstByInventoryInventoryIdAndStatusInOrderByCreatedAtDesc(Long inventoryId, List<String> statuses);
}
