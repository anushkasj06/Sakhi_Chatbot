package com.wms.dtos.response;

import java.math.BigDecimal;
import java.util.Map;

public class PurchaseOrdersSummaryResponse {

    private final Integer totalPurchaseOrders;
    private final BigDecimal totalAmount;
    private final Map<String, Long> statusCounts;

    public PurchaseOrdersSummaryResponse(Integer totalPurchaseOrders, BigDecimal totalAmount, Map<String, Long> statusCounts) {
        this.totalPurchaseOrders = totalPurchaseOrders;
        this.totalAmount = totalAmount;
        this.statusCounts = statusCounts;
    }

    public Integer getTotalPurchaseOrders() {
        return totalPurchaseOrders;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }
}
