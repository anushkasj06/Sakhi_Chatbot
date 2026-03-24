package com.wms.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private final Long paymentId;
    private final Long orderId;
    private final String transactionId;
    private final LocalDateTime paymentDate;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String status;

    public PaymentResponse(
        Long paymentId,
        Long orderId,
        String transactionId,
        LocalDateTime paymentDate,
        BigDecimal amount,
        String paymentMethod,
        String status
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }
}
