package com.wms.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    List<InventoryMovement> findByWarehouseWarehouseIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long warehouseId,
        LocalDateTime from,
        LocalDateTime to
    );
}
