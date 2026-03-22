package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.InventoryAdjustmentRequest;
import com.wms.dtos.request.InventoryTransferRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.InventoryResponse;
import com.wms.dtos.response.InventoryTransferResponse;
import com.wms.services.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'PICKER', 'PACKER', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Inventory fetched", inventoryService.listInventory()));
    }

    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'PICKER', 'PACKER', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> get(@PathVariable Long inventoryId) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory fetched", inventoryService.getInventory(inventoryId)));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'PICKER', 'PACKER', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse inventory fetched", inventoryService.getByWarehouse(warehouseId)));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'PICKER', 'PACKER', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok("Product inventory fetched", inventoryService.getByProduct(productId)));
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK')")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjust(@Valid @RequestBody InventoryAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory adjusted", inventoryService.adjustInventory(request)));
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK')")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> transfer(@Valid @RequestBody InventoryTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory transferred", inventoryService.transferInventory(request)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> lowStock(
        @RequestParam(name = "threshold", required = false) Integer threshold
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Low stock inventory fetched", inventoryService.getLowStock(threshold)));
    }
}
