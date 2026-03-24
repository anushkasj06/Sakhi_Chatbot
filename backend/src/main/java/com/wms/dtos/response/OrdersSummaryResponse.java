package com.wms.dtos.response;

import java.math.BigDecimal;
import java.util.Map;

public class OrdersSummaryResponse {

    private final Integer totalOrders;
    private final BigDecimal totalAmount;
    private final Map<String, Long> statusCounts;

    public OrdersSummaryResponse(Integer totalOrders, BigDecimal totalAmount, Map<String, Long> statusCounts) {
        this.totalOrders = totalOrders;
        this.totalAmount = totalAmount;
        this.statusCounts = statusCounts;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }
}
