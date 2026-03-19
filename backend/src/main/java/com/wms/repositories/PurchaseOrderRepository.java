package com.wms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.PurchaseOrder;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByReceiverUserId(Long userId);
}
