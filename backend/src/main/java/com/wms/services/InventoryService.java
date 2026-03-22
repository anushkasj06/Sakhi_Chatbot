package com.wms.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.InventoryAdjustmentRequest;
import com.wms.dtos.request.InventoryTransferRequest;
import com.wms.dtos.response.InventoryResponse;
import com.wms.dtos.response.InventoryTransferResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Inventory;
import com.wms.models.Product;
import com.wms.models.Warehouse;
import com.wms.repositories.InventoryRepository;
import com.wms.repositories.ProductRepository;
import com.wms.repositories.WarehouseRepository;

@Service
public class InventoryService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(
        InventoryRepository inventoryRepository,
        ProductRepository productRepository,
        WarehouseRepository warehouseRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public List<InventoryResponse> listInventory() {
        return inventoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public InventoryResponse getInventory(Long inventoryId) {
        return toResponse(findInventory(inventoryId));
    }

    public List<InventoryResponse> getByWarehouse(Long warehouseId) {
        ensureWarehouseExists(warehouseId);
        return inventoryRepository.findByWarehouseWarehouseId(warehouseId).stream().map(this::toResponse).toList();
    }

    public List<InventoryResponse> getByProduct(Long productId) {
        ensureProductExists(productId);
        return inventoryRepository.findByProductProductId(productId).stream().map(this::toResponse).toList();
    }

    public List<InventoryResponse> getLowStock(Integer threshold) {
        int resolvedThreshold = threshold == null ? DEFAULT_LOW_STOCK_THRESHOLD : threshold;
        if (resolvedThreshold < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Low stock threshold cannot be negative");
        }
        return inventoryRepository.findByQuantityLessThanEqual(resolvedThreshold).stream().map(this::toResponse).toList();
    }

    @Transactional
    public InventoryResponse adjustInventory(InventoryAdjustmentRequest request) {
        if (request.getQuantityDelta() == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "quantityDelta cannot be zero");
        }

        Product product = findProduct(request.getProductId());
        Warehouse warehouse = findWarehouse(request.getWarehouseId());

        Inventory inventory = inventoryRepository
            .findByProductProductIdAndWarehouseWarehouseId(product.getProductId(), warehouse.getWarehouseId())
            .orElseGet(() -> createInventory(product, warehouse));

        int nextQuantity = inventory.getQuantity() + request.getQuantityDelta();
        if (nextQuantity < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient stock for adjustment");
        }

        inventory.setQuantity(nextQuantity);
        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryTransferResponse transferInventory(InventoryTransferRequest request) {
        if (request.getSourceWarehouseId().equals(request.getTargetWarehouseId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Source and target warehouse must be different");
        }

        Product product = findProduct(request.getProductId());
        Warehouse sourceWarehouse = findWarehouse(request.getSourceWarehouseId());
        Warehouse targetWarehouse = findWarehouse(request.getTargetWarehouseId());

        Inventory sourceInventory = inventoryRepository
            .findByProductProductIdAndWarehouseWarehouseId(product.getProductId(), sourceWarehouse.getWarehouseId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Source inventory not found"));

        if (sourceInventory.getQuantity() < request.getQuantity()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient stock in source warehouse");
        }

        Inventory targetInventory = inventoryRepository
            .findByProductProductIdAndWarehouseWarehouseId(product.getProductId(), targetWarehouse.getWarehouseId())
            .orElseGet(() -> createInventory(product, targetWarehouse));

        sourceInventory.setQuantity(sourceInventory.getQuantity() - request.getQuantity());
        targetInventory.setQuantity(targetInventory.getQuantity() + request.getQuantity());

        Inventory savedSource = inventoryRepository.save(sourceInventory);
        Inventory savedTarget = inventoryRepository.save(targetInventory);

        return new InventoryTransferResponse(
            product.getProductId(),
            request.getQuantity(),
            toResponse(savedSource),
            toResponse(savedTarget)
        );
    }

    private Inventory createInventory(Product product, Warehouse warehouse) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(0);
        inventory.setLocationInWarehouse(null);
        return inventory;
    }

    private Inventory findInventory(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inventory not found"));
    }

    private void ensureWarehouseExists(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found");
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Warehouse findWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
            inventory.getInventoryId(),
            inventory.getProduct().getProductId(),
            inventory.getProduct().getName(),
            inventory.getWarehouse().getWarehouseId(),
            inventory.getWarehouse().getLocation(),
            inventory.getQuantity(),
            inventory.getLocationInWarehouse()
        );
    }
}
