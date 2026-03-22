package com.wms.dtos.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull
    private Long customerId;

    private LocalDateTime deliveryDate;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
