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

import com.wms.dtos.request.CreateOrderItemRequest;
import com.wms.dtos.request.CreateOrderRequest;
import com.wms.dtos.request.UpdateOrderItemRequest;
import com.wms.dtos.request.UpdateOrderRequest;
import com.wms.dtos.request.UpdateOrderStatusRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.OrderResponse;
import com.wms.services.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Order created", orderService.createOrder(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched", orderService.listOrders()));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Order fetched", orderService.getOrder(orderId)));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<OrderResponse>> update(
        @PathVariable Long orderId,
        @Valid @RequestBody UpdateOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Order updated", orderService.updateOrder(orderId, request)));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
        @PathVariable Long orderId,
        @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Order status updated", orderService.updateStatus(orderId, request)));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", orderService.cancelOrder(orderId)));
    }

    @PostMapping("/{orderId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<OrderResponse>> addItem(
        @PathVariable Long orderId,
        @Valid @RequestBody CreateOrderItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Order item added", orderService.addItem(orderId, request)));
    }

    @PutMapping("/{orderId}/items/{orderItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateItem(
        @PathVariable Long orderId,
        @PathVariable Long orderItemId,
        @Valid @RequestBody UpdateOrderItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Order item updated", orderService.updateItem(orderId, orderItemId, request)));
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<OrderResponse>> deleteItem(
        @PathVariable Long orderId,
        @PathVariable Long orderItemId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Order item deleted", orderService.deleteItem(orderId, orderItemId)));
    }
}
