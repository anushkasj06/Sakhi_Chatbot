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

import com.wms.dtos.request.ConfirmPaymentRequest;
import com.wms.dtos.request.CreatePaymentIntentRequest;
import com.wms.dtos.request.RefundPaymentRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.PaymentResponse;
import com.wms.services.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/intent")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment intent created", paymentService.createIntent(request)));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER_SERVICE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirm(@Valid @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed", paymentService.confirm(request)));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER_SERVICE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.ok("Payment fetched", paymentService.getById(paymentId)));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER_SERVICE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("Order payments fetched", paymentService.getByOrderId(orderId)));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(
        @PathVariable Long paymentId,
        @Valid @RequestBody RefundPaymentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Payment refund processed", paymentService.refund(paymentId, request)));
    }
}
