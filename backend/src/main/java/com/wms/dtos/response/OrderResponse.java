package com.wms.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private final Long orderId;
    private final Long customerId;
    private final String customerName;
    private final String customerEmail;
    private final LocalDateTime orderDate;
    private final LocalDateTime deliveryDate;
    private final BigDecimal totalAmount;
    private final String status;
    private final List<OrderItemResponse> items;

    public OrderResponse(
        Long orderId,
        Long customerId,
        String customerName,
        String customerEmail,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        BigDecimal totalAmount,
        String status,
        List<OrderItemResponse> items
    ) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
