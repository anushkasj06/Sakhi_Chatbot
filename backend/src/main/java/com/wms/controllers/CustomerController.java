package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.UpdateCustomerProfileRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.CustomerResponse;
import com.wms.services.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.ok("Customer profile fetched", customerService.getMyProfile()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateMyProfile(@Valid @RequestBody UpdateCustomerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Customer profile updated", customerService.updateMyProfile(request)));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.ok("Customer fetched", customerService.getCustomer(customerId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> listCustomers() {
        return ResponseEntity.ok(ApiResponse.ok("Customers fetched", customerService.listCustomers()));
    }
}
