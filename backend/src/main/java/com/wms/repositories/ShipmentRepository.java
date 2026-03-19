package com.wms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByPickerUserId(Long userId);

    List<Shipment> findByPackerUserId(Long userId);
}
