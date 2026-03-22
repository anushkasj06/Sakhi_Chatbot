package com.wms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.PurchaseOrderItem;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    List<PurchaseOrderItem> findByPurchaseOrderPoId(Long poId);

    Optional<PurchaseOrderItem> findByPoItemIdAndPurchaseOrderPoId(Long poItemId, Long poId);
}
