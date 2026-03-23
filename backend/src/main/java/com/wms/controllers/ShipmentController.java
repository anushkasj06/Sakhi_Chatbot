package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.AssignShipmentUserRequest;
import com.wms.dtos.request.CreateShipmentRequest;
import com.wms.dtos.request.UpdateShipmentStatusRequest;
import com.wms.dtos.request.UpdateShipmentTrackingRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.ShipmentAssignmentsResponse;
import com.wms.dtos.response.ShipmentResponse;
import com.wms.services.ShipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> create(@Valid @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment created", shipmentService.createShipment(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'PICKER', 'PACKER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Shipments fetched", shipmentService.listShipments()));
    }

    @GetMapping("/{shipmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'PICKER', 'PACKER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> get(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment fetched", shipmentService.getShipment(shipmentId)));
    }

    @PatchMapping("/{shipmentId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateStatus(
        @PathVariable Long shipmentId,
        @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment status updated", shipmentService.updateStatus(shipmentId, request)));
    }

    @PatchMapping("/{shipmentId}/tracking")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateTracking(
        @PathVariable Long shipmentId,
        @Valid @RequestBody UpdateShipmentTrackingRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment tracking updated", shipmentService.updateTracking(shipmentId, request)));
    }

    @PatchMapping("/{shipmentId}/assign-picker")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> assignPicker(
        @PathVariable Long shipmentId,
        @RequestBody AssignShipmentUserRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment picker assigned", shipmentService.assignPicker(shipmentId, request)));
    }

    @PatchMapping("/{shipmentId}/assign-packer")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> assignPacker(
        @PathVariable Long shipmentId,
        @RequestBody AssignShipmentUserRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment packer assigned", shipmentService.assignPacker(shipmentId, request)));
    }

    @GetMapping("/{shipmentId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'PICKER', 'PACKER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<ShipmentAssignmentsResponse>> getAssignments(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment assignments fetched", shipmentService.getAssignments(shipmentId)));
    }

    @PostMapping("/{shipmentId}/mark-picked")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PICKER')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> markPicked(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment marked picked", shipmentService.markPicked(shipmentId)));
    }

    @PostMapping("/{shipmentId}/mark-packed")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PACKER')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> markPacked(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment marked packed", shipmentService.markPacked(shipmentId)));
    }

    @PostMapping("/{shipmentId}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PACKER')")
    public ResponseEntity<ApiResponse<ShipmentResponse>> dispatch(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok("Shipment dispatched", shipmentService.dispatch(shipmentId)));
    }
}
