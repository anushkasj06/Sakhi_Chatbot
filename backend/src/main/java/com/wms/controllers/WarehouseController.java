package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.AssignWarehouseManagerRequest;
import com.wms.dtos.request.CreateWarehouseRequest;
import com.wms.dtos.request.UpdateWarehouseRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.UserSummaryResponse;
import com.wms.dtos.response.WarehouseResponse;
import com.wms.services.WarehouseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> create(@Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse created", warehouseService.createWarehouse(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Warehouses fetched", warehouseService.listWarehouses()));
    }

    @GetMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> get(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse fetched", warehouseService.getWarehouse(warehouseId)));
    }

    @PutMapping("/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> update(@PathVariable Long warehouseId, @Valid @RequestBody UpdateWarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse updated", warehouseService.updateWarehouse(warehouseId, request)));
    }

    @PatchMapping("/{warehouseId}/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> assignManager(
        @PathVariable Long warehouseId,
        @RequestBody AssignWarehouseManagerRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse manager updated", warehouseService.assignManager(warehouseId, request)));
    }

    @GetMapping("/{warehouseId}/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getManager(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse manager fetched", warehouseService.getManager(warehouseId)));
    }

    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long warehouseId) {
        warehouseService.deleteWarehouse(warehouseId);
        return ResponseEntity.ok(ApiResponse.ok("Warehouse deleted", null));
    }
}
