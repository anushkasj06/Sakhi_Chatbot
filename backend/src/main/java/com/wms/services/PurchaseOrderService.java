package com.wms.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreatePurchaseOrderItemRequest;
import com.wms.dtos.request.CreatePurchaseOrderRequest;
import com.wms.dtos.request.ReceivePurchaseOrderItemRequest;
import com.wms.dtos.request.ReceivePurchaseOrderRequest;
import com.wms.dtos.request.UpdatePurchaseOrderItemRequest;
import com.wms.dtos.request.UpdatePurchaseOrderRequest;
import com.wms.dtos.request.UpdatePurchaseOrderStatusRequest;
import com.wms.dtos.response.PurchaseOrderItemResponse;
import com.wms.dtos.response.PurchaseOrderResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Inventory;
import com.wms.models.Product;
import com.wms.models.PurchaseOrder;
import com.wms.models.PurchaseOrderItem;
import com.wms.models.Supplier;
import com.wms.models.User;
import com.wms.models.Warehouse;
import com.wms.repositories.InventoryRepository;
import com.wms.repositories.ProductRepository;
import com.wms.repositories.PurchaseOrderItemRepository;
import com.wms.repositories.PurchaseOrderRepository;
import com.wms.repositories.SupplierRepository;
import com.wms.repositories.UserRepository;
import com.wms.repositories.WarehouseRepository;

@Service
public class PurchaseOrderService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_PARTIALLY_RECEIVED = "PARTIALLY_RECEIVED";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public PurchaseOrderService(
        PurchaseOrderRepository purchaseOrderRepository,
        PurchaseOrderItemRepository purchaseOrderItemRepository,
        SupplierRepository supplierRepository,
        WarehouseRepository warehouseRepository,
        ProductRepository productRepository,
        InventoryRepository inventoryRepository,
        UserRepository userRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(findSupplier(request.getSupplierId()));
        purchaseOrder.setWarehouse(findWarehouse(request.getWarehouseId()));
        purchaseOrder.setOrderDate(LocalDateTime.now());
        purchaseOrder.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        purchaseOrder.setTotalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        purchaseOrder.setStatus(STATUS_DRAFT);
        purchaseOrder.setReceiver(null);
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    public List<PurchaseOrderResponse> listPurchaseOrders() {
        return purchaseOrderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PurchaseOrderResponse getPurchaseOrder(Long poId) {
        return toResponse(findPurchaseOrder(poId));
    }

    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(Long poId, UpdatePurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        ensureNotFinalized(purchaseOrder);

        purchaseOrder.setSupplier(findSupplier(request.getSupplierId()));
        purchaseOrder.setWarehouse(findWarehouse(request.getWarehouseId()));
        purchaseOrder.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        purchaseOrder.setStatus(normalizeStatus(request.getStatus()));

        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrderResponse updateStatus(Long poId, UpdatePurchaseOrderStatusRequest request) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        purchaseOrder.setStatus(normalizeStatus(request.getStatus()));
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrderResponse submitPurchaseOrder(Long poId) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        if (purchaseOrderItemRepository.findByPurchaseOrderPoId(poId).isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Purchase order must contain at least one item before submit");
        }

        String status = purchaseOrder.getStatus();
        if (!STATUS_DRAFT.equals(status) && !STATUS_APPROVED.equals(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only DRAFT or APPROVED purchase orders can be submitted");
        }

        purchaseOrder.setStatus(STATUS_SUBMITTED);
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long poId) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        if (STATUS_RECEIVED.equals(purchaseOrder.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Received purchase order cannot be cancelled");
        }

        purchaseOrder.setStatus(STATUS_CANCELLED);
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrderResponse addItem(Long poId, CreatePurchaseOrderItemRequest request) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        ensureNotFinalized(purchaseOrder);

        PurchaseOrderItem poItem = new PurchaseOrderItem();
        poItem.setPurchaseOrder(purchaseOrder);
        poItem.setProduct(findProduct(request.getProductId()));
        poItem.setQuantity(request.getQuantity());
        poItem.setUnitPrice(normalizeAmount(request.getUnitPrice()));
        purchaseOrderItemRepository.save(poItem);

        recalculateTotal(purchaseOrder);
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrderResponse updateItem(Long poId, Long poItemId, UpdatePurchaseOrderItemRequest request) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        ensureNotFinalized(purchaseOrder);

        PurchaseOrderItem poItem = purchaseOrderItemRepository
            .findByPoItemIdAndPurchaseOrderPoId(poItemId, poId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Purchase order item not found"));

        poItem.setProduct(findProduct(request.getProductId()));
        poItem.setQuantity(request.getQuantity());
        poItem.setUnitPrice(normalizeAmount(request.getUnitPrice()));
        purchaseOrderItemRepository.save(poItem);

        recalculateTotal(purchaseOrder);
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrderResponse deleteItem(Long poId, Long poItemId) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        ensureNotFinalized(purchaseOrder);

        PurchaseOrderItem poItem = purchaseOrderItemRepository
            .findByPoItemIdAndPurchaseOrderPoId(poItemId, poId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Purchase order item not found"));

        purchaseOrderItemRepository.delete(poItem);
        recalculateTotal(purchaseOrder);
        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrderResponse receive(Long poId, ReceivePurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(poId);
        String status = purchaseOrder.getStatus();
        if (!STATUS_SUBMITTED.equals(status) && !STATUS_APPROVED.equals(status) && !STATUS_PARTIALLY_RECEIVED.equals(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Purchase order is not in receivable state");
        }

        List<PurchaseOrderItem> poItems = purchaseOrderItemRepository.findByPurchaseOrderPoId(poId);
        if (poItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Purchase order has no items to receive");
        }

        Map<Long, PurchaseOrderItem> poItemMap = poItems.stream()
            .collect(Collectors.toMap(PurchaseOrderItem::getPoItemId, Function.identity()));

        Map<Long, Integer> receivedMap = request.getItems().stream()
            .collect(Collectors.toMap(
                ReceivePurchaseOrderItemRequest::getPoItemId,
                ReceivePurchaseOrderItemRequest::getReceivedQuantity,
                Integer::sum
            ));

        for (Map.Entry<Long, Integer> entry : receivedMap.entrySet()) {
            PurchaseOrderItem poItem = poItemMap.get(entry.getKey());
            if (poItem == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid purchase order item in receive payload");
            }

            int receivedQty = entry.getValue();
            if (receivedQty <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Received quantity must be greater than zero");
            }
            if (receivedQty > poItem.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Received quantity exceeds ordered quantity");
            }

            Inventory inventory = inventoryRepository
                .findByProductProductIdAndWarehouseWarehouseId(
                    poItem.getProduct().getProductId(),
                    purchaseOrder.getWarehouse().getWarehouseId()
                )
                .orElseGet(() -> createInventory(poItem.getProduct(), purchaseOrder.getWarehouse()));

            inventory.setQuantity(inventory.getQuantity() + receivedQty);
            inventoryRepository.save(inventory);
        }

        boolean fullyReceived = poItems.stream().allMatch(item -> {
            int qty = receivedMap.getOrDefault(item.getPoItemId(), 0);
            return qty == item.getQuantity();
        });

        purchaseOrder.setReceiver(getCurrentUser());
        purchaseOrder.setStatus(fullyReceived ? STATUS_RECEIVED : STATUS_PARTIALLY_RECEIVED);

        return toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    private PurchaseOrder findPurchaseOrder(Long poId) {
        return purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    private Warehouse findWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        String normalized = status.trim().toUpperCase();
        if (!normalized.equals(STATUS_DRAFT)
            && !normalized.equals(STATUS_APPROVED)
            && !normalized.equals(STATUS_SUBMITTED)
            && !normalized.equals(STATUS_PARTIALLY_RECEIVED)
            && !normalized.equals(STATUS_RECEIVED)
            && !normalized.equals(STATUS_CANCELLED)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid purchase order status");
        }
        return normalized;
    }

    private void ensureNotFinalized(PurchaseOrder purchaseOrder) {
        if (STATUS_CANCELLED.equals(purchaseOrder.getStatus()) || STATUS_RECEIVED.equals(purchaseOrder.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Finalized purchase order cannot be modified");
        }
    }

    private void recalculateTotal(PurchaseOrder purchaseOrder) {
        BigDecimal total = purchaseOrderItemRepository.findByPurchaseOrderPoId(purchaseOrder.getPoId()).stream()
            .map(item -> normalizeAmount(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        purchaseOrder.setTotalAmount(total);
    }

    private Inventory createInventory(Product product, Warehouse warehouse) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(0);
        inventory.setLocationInWarehouse(null);
        return inventory;
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderItemResponse> items = purchaseOrderItemRepository.findByPurchaseOrderPoId(purchaseOrder.getPoId())
            .stream()
            .map(this::toItemResponse)
            .toList();

        return new PurchaseOrderResponse(
            purchaseOrder.getPoId(),
            purchaseOrder.getSupplier().getSupplierId(),
            purchaseOrder.getSupplier().getSName(),
            purchaseOrder.getWarehouse().getWarehouseId(),
            purchaseOrder.getWarehouse().getLocation(),
            purchaseOrder.getOrderDate(),
            purchaseOrder.getExpectedDeliveryDate(),
            normalizeAmount(purchaseOrder.getTotalAmount()),
            purchaseOrder.getStatus(),
            purchaseOrder.getReceiver() != null ? purchaseOrder.getReceiver().getUserId() : null,
            purchaseOrder.getReceiver() != null ? purchaseOrder.getReceiver().getName() : null,
            items
        );
    }

    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        BigDecimal unitPrice = normalizeAmount(item.getUnitPrice());
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);

        return new PurchaseOrderItemResponse(
            item.getPoItemId(),
            item.getProduct().getProductId(),
            item.getProduct().getName(),
            item.getQuantity(),
            unitPrice,
            lineTotal
        );
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be a valid non-negative number");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
