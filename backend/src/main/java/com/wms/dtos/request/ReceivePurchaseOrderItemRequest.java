package com.wms.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReceivePurchaseOrderItemRequest {

    @NotNull
    private Long poItemId;

    @NotNull
    @Min(1)
    private Integer receivedQuantity;

    public Long getPoItemId() {
        return poItemId;
    }

    public void setPoItemId(Long poItemId) {
        this.poItemId = poItemId;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
}
