package com.wms.dtos.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class ReceivePurchaseOrderRequest {

    @Valid
    @NotEmpty
    private List<ReceivePurchaseOrderItemRequest> items;

    public List<ReceivePurchaseOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReceivePurchaseOrderItemRequest> items) {
        this.items = items;
    }
}
