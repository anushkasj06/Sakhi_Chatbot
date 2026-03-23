package com.wms.dtos.response;

import java.math.BigDecimal;

public class OrderItemResponse {

    private final Long orderItemId;
    private final Long productId;
    private final String productName;
    private final Integer quantity;
    private final BigDecimal pricePerUnit;
    private final BigDecimal lineTotal;

    public OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal pricePerUnit,
        BigDecimal lineTotal
    ) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.lineTotal = lineTotal;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
