package com.wms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findAllByOrderByShipmentDateDesc();

    List<Shipment> findByPickerUserId(Long userId);

    List<Shipment> findByPackerUserId(Long userId);

    Optional<Shipment> findByTrackingNumberIgnoreCase(String trackingNumber);

    boolean existsByTrackingNumberIgnoreCase(String trackingNumber);

    boolean existsByTrackingNumberIgnoreCaseAndShipmentIdNot(String trackingNumber, Long shipmentId);
}
