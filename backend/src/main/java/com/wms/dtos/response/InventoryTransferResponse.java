package com.wms.dtos.response;

public class InventoryTransferResponse {

    private final Long productId;
    private final Integer transferredQuantity;
    private final InventoryResponse sourceInventory;
    private final InventoryResponse targetInventory;

    public InventoryTransferResponse(
        Long productId,
        Integer transferredQuantity,
        InventoryResponse sourceInventory,
        InventoryResponse targetInventory
    ) {
        this.productId = productId;
        this.transferredQuantity = transferredQuantity;
        this.sourceInventory = sourceInventory;
        this.targetInventory = targetInventory;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getTransferredQuantity() {
        return transferredQuantity;
    }

    public InventoryResponse getSourceInventory() {
        return sourceInventory;
    }

    public InventoryResponse getTargetInventory() {
        return targetInventory;
    }
}
