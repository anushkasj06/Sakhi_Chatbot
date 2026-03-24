package com.wms.dtos.response;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentsReconciliationResponse {

    private final Integer totalPayments;
    private final BigDecimal totalAmount;
    private final BigDecimal capturedAmount;
    private final BigDecimal refundedAmount;
    private final Map<String, Long> statusCounts;

    public PaymentsReconciliationResponse(
        Integer totalPayments,
        BigDecimal totalAmount,
        BigDecimal capturedAmount,
        BigDecimal refundedAmount,
        Map<String, Long> statusCounts
    ) {
        this.totalPayments = totalPayments;
        this.totalAmount = totalAmount;
        this.capturedAmount = capturedAmount;
        this.refundedAmount = refundedAmount;
        this.statusCounts = statusCounts;
    }

    public Integer getTotalPayments() {
        return totalPayments;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getCapturedAmount() {
        return capturedAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }
}
