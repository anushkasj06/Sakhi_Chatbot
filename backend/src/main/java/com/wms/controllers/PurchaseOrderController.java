package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.CreatePurchaseOrderItemRequest;
import com.wms.dtos.request.CreatePurchaseOrderRequest;
import com.wms.dtos.request.ReceivePurchaseOrderRequest;
import com.wms.dtos.request.UpdatePurchaseOrderItemRequest;
import com.wms.dtos.request.UpdatePurchaseOrderRequest;
import com.wms.dtos.request.UpdatePurchaseOrderStatusRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.PurchaseOrderResponse;
import com.wms.services.PurchaseOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> create(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order created", purchaseOrderService.createPurchaseOrder(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Purchase orders fetched", purchaseOrderService.listPurchaseOrders()));
    }

    @GetMapping("/{poId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> get(@PathVariable Long poId) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order fetched", purchaseOrderService.getPurchaseOrder(poId)));
    }

    @PutMapping("/{poId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> update(
        @PathVariable Long poId,
        @Valid @RequestBody UpdatePurchaseOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order updated", purchaseOrderService.updatePurchaseOrder(poId, request)));
    }

    @PatchMapping("/{poId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updateStatus(
        @PathVariable Long poId,
        @Valid @RequestBody UpdatePurchaseOrderStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order status updated", purchaseOrderService.updateStatus(poId, request)));
    }

    @PostMapping("/{poId}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> submit(@PathVariable Long poId) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order submitted", purchaseOrderService.submitPurchaseOrder(poId)));
    }

    @PostMapping("/{poId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> cancel(@PathVariable Long poId) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order cancelled", purchaseOrderService.cancelPurchaseOrder(poId)));
    }

    @PostMapping("/{poId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> addItem(
        @PathVariable Long poId,
        @Valid @RequestBody CreatePurchaseOrderItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order item added", purchaseOrderService.addItem(poId, request)));
    }

    @PutMapping("/{poId}/items/{poItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updateItem(
        @PathVariable Long poId,
        @PathVariable Long poItemId,
        @Valid @RequestBody UpdatePurchaseOrderItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order item updated", purchaseOrderService.updateItem(poId, poItemId, request)));
    }

    @DeleteMapping("/{poId}/items/{poItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> deleteItem(
        @PathVariable Long poId,
        @PathVariable Long poItemId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order item deleted", purchaseOrderService.deleteItem(poId, poItemId)));
    }

    @PostMapping("/{poId}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> receive(
        @PathVariable Long poId,
        @Valid @RequestBody ReceivePurchaseOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order received", purchaseOrderService.receive(poId, request)));
    }
}
