package com.wms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByWarehouseWarehouseId(Long warehouseId);

    List<Inventory> findByProductProductId(Long productId);

    List<Inventory> findByQuantityLessThanEqual(Integer threshold);

    Optional<Inventory> findByProductProductIdAndWarehouseWarehouseId(Long productId, Long warehouseId);
}
