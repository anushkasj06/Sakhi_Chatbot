package com.wms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderOrderIdOrderByPaymentDateDesc(Long orderId);

    boolean existsByTransactionId(String transactionId);
}
