package com.wms.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wms.dtos.response.InventoryMovementResponse;
import com.wms.dtos.response.InventorySummaryResponse;
import com.wms.dtos.response.OrdersSummaryResponse;
import com.wms.dtos.response.PaymentsReconciliationResponse;
import com.wms.dtos.response.PurchaseOrdersSummaryResponse;
import com.wms.dtos.response.ShipmentsSummaryResponse;
import com.wms.models.Inventory;
import com.wms.models.InventoryMovement;
import com.wms.models.Order;
import com.wms.models.Payment;
import com.wms.models.PurchaseOrder;
import com.wms.models.Shipment;
import com.wms.models.Warehouse;
import com.wms.repositories.InventoryMovementRepository;
import com.wms.repositories.InventoryRepository;
import com.wms.repositories.OrderRepository;
import com.wms.repositories.PaymentRepository;
import com.wms.repositories.PurchaseOrderRepository;
import com.wms.repositories.ShipmentRepository;
import com.wms.repositories.WarehouseRepository;

@Service
public class ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PaymentRepository paymentRepository;
    private final WarehouseRepository warehouseRepository;

    public ReportingService(
        InventoryRepository inventoryRepository,
        InventoryMovementRepository inventoryMovementRepository,
        OrderRepository orderRepository,
        ShipmentRepository shipmentRepository,
        PurchaseOrderRepository purchaseOrderRepository,
        PaymentRepository paymentRepository,
        WarehouseRepository warehouseRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.paymentRepository = paymentRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public List<InventorySummaryResponse> inventorySummary(Long warehouseId) {
        List<Warehouse> warehouses;
        if (warehouseId != null) {
            warehouses = warehouseRepository.findById(warehouseId).stream().toList();
        } else {
            warehouses = warehouseRepository.findAll();
        }

        List<InventorySummaryResponse> summary = warehouses.stream().map(warehouse -> {
            List<Inventory> inventoryList = inventoryRepository.findByWarehouseWarehouseId(warehouse.getWarehouseId());
            int totalQty = inventoryList.stream().mapToInt(Inventory::getQuantity).sum();
            int lowStock = (int) inventoryList.stream().filter(i -> i.getQuantity() <= 10).count();

            return new InventorySummaryResponse(
                warehouse.getWarehouseId(),
                warehouse.getLocation(),
                inventoryList.size(),
                totalQty,
                lowStock
            );
        }).toList();
        log.info("Generated inventory summary report: warehouseFilter={}, records={}", warehouseId, summary.size());
        return summary;
    }

    public List<InventoryMovementResponse> inventoryMovements(LocalDate fromDate, LocalDate toDate, Long warehouseId) {
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime to = toDate != null ? toDate.plusDays(1).atStartOfDay().minusNanos(1) : LocalDateTime.now();

        List<InventoryMovement> movements = warehouseId == null
            ? inventoryMovementRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to)
            : inventoryMovementRepository.findByWarehouseWarehouseIdAndCreatedAtBetweenOrderByCreatedAtDesc(warehouseId, from, to);

        List<InventoryMovementResponse> response = movements.stream().map(m -> new InventoryMovementResponse(
            m.getMovementId(),
            m.getCreatedAt(),
            m.getWarehouse().getWarehouseId(),
            m.getWarehouse().getLocation(),
            m.getProduct().getProductId(),
            m.getProduct().getName(),
            m.getQuantityDelta(),
            m.getMovementType(),
            m.getReason(),
            m.getReferenceType(),
            m.getReferenceId()
        )).toList();
        log.info("Generated inventory movements report: warehouseFilter={}, fromDate={}, toDate={}, records={}", warehouseId, fromDate, toDate, response.size());
        return response;
    }

    public OrdersSummaryResponse ordersSummary(LocalDate fromDate, LocalDate toDate, String status, Long customerId) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        orders = orders.stream()
            .filter(o -> customerId == null || o.getCustomer().getCustomerId().equals(customerId))
            .filter(o -> status == null || status.isBlank() || o.getStatus().equalsIgnoreCase(status.trim()))
            .filter(o -> isWithinRange(o.getOrderDate(), fromDate, toDate))
            .toList();

        BigDecimal totalAmount = orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Map<String, Long> statusCounts = orders.stream()
            .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        OrdersSummaryResponse response = new OrdersSummaryResponse(orders.size(), totalAmount, statusCounts);
        log.info("Generated orders summary report: customerFilter={}, statusFilter={}, fromDate={}, toDate={}, totalOrders={}", customerId, status, fromDate, toDate, response.getTotalOrders());
        return response;
    }

    public ShipmentsSummaryResponse shipmentsSummary(LocalDate fromDate, LocalDate toDate, String status, Long warehouseId) {
        List<Shipment> shipments = shipmentRepository.findAllByOrderByShipmentDateDesc();
        shipments = shipments.stream()
            .filter(s -> warehouseId == null || s.getWarehouse().getWarehouseId().equals(warehouseId))
            .filter(s -> status == null || status.isBlank() || s.getStatus().equalsIgnoreCase(status.trim()))
            .filter(s -> isWithinRange(s.getShipmentDate(), fromDate, toDate))
            .toList();

        Map<String, Long> statusCounts = shipments.stream()
            .collect(Collectors.groupingBy(Shipment::getStatus, Collectors.counting()));

        ShipmentsSummaryResponse response = new ShipmentsSummaryResponse(shipments.size(), statusCounts);
        log.info("Generated shipments summary report: warehouseFilter={}, statusFilter={}, fromDate={}, toDate={}, totalShipments={}", warehouseId, status, fromDate, toDate, response.getTotalShipments());
        return response;
    }

    public PurchaseOrdersSummaryResponse purchaseOrdersSummary(
        LocalDate fromDate,
        LocalDate toDate,
        String status,
        Long warehouseId,
        Long supplierId
    ) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        purchaseOrders = purchaseOrders.stream()
            .filter(po -> warehouseId == null || po.getWarehouse().getWarehouseId().equals(warehouseId))
            .filter(po -> supplierId == null || po.getSupplier().getSupplierId().equals(supplierId))
            .filter(po -> status == null || status.isBlank() || po.getStatus().equalsIgnoreCase(status.trim()))
            .filter(po -> isWithinRange(po.getOrderDate(), fromDate, toDate))
            .toList();

        BigDecimal totalAmount = purchaseOrders.stream()
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Map<String, Long> statusCounts = purchaseOrders.stream()
            .collect(Collectors.groupingBy(PurchaseOrder::getStatus, Collectors.counting()));

        PurchaseOrdersSummaryResponse response = new PurchaseOrdersSummaryResponse(purchaseOrders.size(), totalAmount, statusCounts);
        log.info("Generated purchase orders summary report: warehouseFilter={}, supplierFilter={}, statusFilter={}, fromDate={}, toDate={}, totalPurchaseOrders={}", warehouseId, supplierId, status, fromDate, toDate, response.getTotalPurchaseOrders());
        return response;
    }

    public PaymentsReconciliationResponse paymentsReconciliation(LocalDate fromDate, LocalDate toDate, String status, Long orderId) {
        List<Payment> payments = paymentRepository.findAll();
        payments = payments.stream()
            .filter(p -> orderId == null || p.getOrder().getOrderId().equals(orderId))
            .filter(p -> status == null || status.isBlank() || p.getStatus().equalsIgnoreCase(status.trim()))
            .filter(p -> isWithinRange(p.getPaymentDate(), fromDate, toDate))
            .toList();

        BigDecimal totalAmount = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal captured = payments.stream()
            .filter(p -> "CAPTURED".equalsIgnoreCase(p.getStatus()))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal refunded = payments.stream()
            .filter(p -> "REFUNDED".equalsIgnoreCase(p.getStatus()) || "PARTIALLY_REFUNDED".equalsIgnoreCase(p.getStatus()))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Map<String, Long> statusCounts = payments.stream()
            .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));

        PaymentsReconciliationResponse response = new PaymentsReconciliationResponse(payments.size(), totalAmount, captured, refunded, statusCounts);
        log.info("Generated payments reconciliation report: orderFilter={}, statusFilter={}, fromDate={}, toDate={}, totalPayments={}", orderId, status, fromDate, toDate, response.getTotalPayments());
        return response;
    }

    private boolean isWithinRange(LocalDateTime dateTime, LocalDate fromDate, LocalDate toDate) {
        if (dateTime == null) {
            return false;
        }

        LocalDate date = dateTime.toLocalDate();
        if (fromDate != null && date.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && date.isAfter(toDate)) {
            return false;
        }
        return true;
    }
}
