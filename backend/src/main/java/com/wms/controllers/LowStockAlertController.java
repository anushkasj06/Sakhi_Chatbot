package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.LowStockAlertResponse;
import com.wms.services.LowStockAlertService;

@RestController
@RequestMapping("/api/v1/alerts/low-stock")
public class LowStockAlertController {

    private final LowStockAlertService lowStockAlertService;

    public LowStockAlertController(LowStockAlertService lowStockAlertService) {
        this.lowStockAlertService = lowStockAlertService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<LowStockAlertResponse>>> listAlerts(
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "mineOnly", required = false, defaultValue = "false") boolean mineOnly
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Low stock alerts fetched", lowStockAlertService.listAlerts(status, mineOnly)));
    }

    @PostMapping("/{alertId}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK')")
    public ResponseEntity<ApiResponse<LowStockAlertResponse>> acknowledge(@PathVariable Long alertId) {
        return ResponseEntity.ok(ApiResponse.ok("Low stock alert acknowledged", lowStockAlertService.acknowledgeAlert(alertId)));
    }

    @PostMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK')")
    public ResponseEntity<ApiResponse<LowStockAlertResponse>> resolve(@PathVariable Long alertId) {
        return ResponseEntity.ok(ApiResponse.ok("Low stock alert resolved", lowStockAlertService.resolveAlert(alertId)));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<Integer>> scanNow() {
        return ResponseEntity.ok(ApiResponse.ok("Low stock scan completed", lowStockAlertService.runLowStockScan()));
    }
}
