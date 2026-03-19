package com.wms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndWarehouseIdNot(String email, Long warehouseId);

    List<Warehouse> findByManagerUserId(Long userId);
}
