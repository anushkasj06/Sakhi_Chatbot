package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.CreateCustomerOrderRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.CustomerOrderResponse;
import com.wms.services.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customer/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {

    private final CustomerService customerService;

    public CustomerOrderController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerOrderResponse>> createOrder(@Valid @RequestBody CreateCustomerOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Order created", customerService.createMyOrder(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerOrderResponse>>> listOrders() {
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched", customerService.listMyOrders()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<CustomerOrderResponse>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Order fetched", customerService.getMyOrder(orderId)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CustomerOrderResponse>> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", customerService.cancelMyOrder(orderId)));
    }
}
