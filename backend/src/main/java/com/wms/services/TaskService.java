package com.wms.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.wms.dtos.response.WorkforceTaskResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.PurchaseOrder;
import com.wms.models.Shipment;
import com.wms.models.User;
import com.wms.repositories.PurchaseOrderRepository;
import com.wms.repositories.ShipmentRepository;
import com.wms.repositories.UserRepository;

@Service
public class TaskService {

    private final ShipmentRepository shipmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UserRepository userRepository;

    public TaskService(
        ShipmentRepository shipmentRepository,
        PurchaseOrderRepository purchaseOrderRepository,
        UserRepository userRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.userRepository = userRepository;
    }

    public List<WorkforceTaskResponse> getMyTasks() {
        User user = getCurrentUser();
        List<WorkforceTaskResponse> tasks = new ArrayList<>();
        tasks.addAll(getPickingTasks(user.getUserId()));
        tasks.addAll(getPackingTasks(user.getUserId()));
        tasks.addAll(getReceivingTasks(user.getUserId()));
        tasks.sort(Comparator.comparing(WorkforceTaskResponse::getTaskDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return tasks;
    }

    public List<WorkforceTaskResponse> getPickingTasks() {
        return getPickingTasks(getCurrentUser().getUserId());
    }

    public List<WorkforceTaskResponse> getPackingTasks() {
        return getPackingTasks(getCurrentUser().getUserId());
    }

    public List<WorkforceTaskResponse> getReceivingTasks() {
        return getReceivingTasks(getCurrentUser().getUserId());
    }

    private List<WorkforceTaskResponse> getPickingTasks(Long userId) {
        return shipmentRepository.findByPickerUserId(userId).stream()
            .map(this::toPickingTask)
            .toList();
    }

    private List<WorkforceTaskResponse> getPackingTasks(Long userId) {
        return shipmentRepository.findByPackerUserId(userId).stream()
            .map(this::toPackingTask)
            .toList();
    }

    private List<WorkforceTaskResponse> getReceivingTasks(Long userId) {
        return purchaseOrderRepository.findByReceiverUserId(userId).stream()
            .map(this::toReceivingTask)
            .toList();
    }

    private WorkforceTaskResponse toPickingTask(Shipment shipment) {
        return new WorkforceTaskResponse(
            "PICKING",
            shipment.getShipmentId(),
            shipment.getTrackingNumber(),
            shipment.getStatus(),
            shipment.getWarehouse().getWarehouseId(),
            shipment.getWarehouse().getLocation(),
            shipment.getShipmentDate()
        );
    }

    private WorkforceTaskResponse toPackingTask(Shipment shipment) {
        return new WorkforceTaskResponse(
            "PACKING",
            shipment.getShipmentId(),
            shipment.getTrackingNumber(),
            shipment.getStatus(),
            shipment.getWarehouse().getWarehouseId(),
            shipment.getWarehouse().getLocation(),
            shipment.getShipmentDate()
        );
    }

    private WorkforceTaskResponse toReceivingTask(PurchaseOrder purchaseOrder) {
        return new WorkforceTaskResponse(
            "RECEIVING",
            purchaseOrder.getPoId(),
            "PO-" + purchaseOrder.getPoId(),
            purchaseOrder.getStatus(),
            purchaseOrder.getWarehouse().getWarehouseId(),
            purchaseOrder.getWarehouse().getLocation(),
            purchaseOrder.getOrderDate()
        );
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
