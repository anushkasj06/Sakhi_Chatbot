package com.wms.dtos.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class CreateCustomerOrderRequest {

    private LocalDateTime deliveryDate;

    @Valid
    @NotEmpty
    private List<CustomerOrderItemRequest> items;

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public List<CustomerOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CustomerOrderItemRequest> items) {
        this.items = items;
    }
}
