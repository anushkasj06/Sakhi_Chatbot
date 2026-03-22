package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.CreateSupplierRequest;
import com.wms.dtos.request.UpdateSupplierRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.ProductResponse;
import com.wms.dtos.response.SupplierResponse;
import com.wms.services.SupplierService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier created", supplierService.createSupplier(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Suppliers fetched", supplierService.listSuppliers()));
    }

    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<SupplierResponse>> get(@PathVariable Long supplierId) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier fetched", supplierService.getSupplier(supplierId)));
    }

    @PutMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(
        @PathVariable Long supplierId,
        @Valid @RequestBody UpdateSupplierRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier updated", supplierService.updateSupplier(supplierId, request)));
    }

    @DeleteMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long supplierId) {
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.ok("Supplier deleted", null));
    }

    @GetMapping("/{supplierId}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(@PathVariable Long supplierId) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier products fetched", supplierService.getSupplierProducts(supplierId)));
    }
}
