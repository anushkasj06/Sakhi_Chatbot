package com.wms.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.InventoryMovementResponse;
import com.wms.dtos.response.InventorySummaryResponse;
import com.wms.dtos.response.OrdersSummaryResponse;
import com.wms.dtos.response.PaymentsReconciliationResponse;
import com.wms.dtos.response.PurchaseOrdersSummaryResponse;
import com.wms.dtos.response.ShipmentsSummaryResponse;
import com.wms.services.ReportingService;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<InventorySummaryResponse>>> inventorySummary(
        @RequestParam(name = "warehouseId", required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory summary fetched", reportingService.inventorySummary(warehouseId)));
    }

    @GetMapping("/inventory-movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> inventoryMovements(
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "warehouseId", required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Inventory movements fetched",
            reportingService.inventoryMovements(fromDate, toDate, warehouseId)
        ));
    }

    @GetMapping("/orders-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<OrdersSummaryResponse>> ordersSummary(
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "customerId", required = false) Long customerId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Orders summary fetched",
            reportingService.ordersSummary(fromDate, toDate, status, customerId)
        ));
    }

    @GetMapping("/shipments-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<ShipmentsSummaryResponse>> shipmentsSummary(
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "warehouseId", required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Shipments summary fetched",
            reportingService.shipmentsSummary(fromDate, toDate, status, warehouseId)
        ));
    }

    @GetMapping("/purchase-orders-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<PurchaseOrdersSummaryResponse>> purchaseOrdersSummary(
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "warehouseId", required = false) Long warehouseId,
        @RequestParam(name = "supplierId", required = false) Long supplierId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Purchase orders summary fetched",
            reportingService.purchaseOrdersSummary(fromDate, toDate, status, warehouseId, supplierId)
        ));
    }

    @GetMapping("/payments-reconciliation")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<PaymentsReconciliationResponse>> paymentsReconciliation(
        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "orderId", required = false) Long orderId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
            "Payments reconciliation fetched",
            reportingService.paymentsReconciliation(fromDate, toDate, status, orderId)
        ));
    }
}
