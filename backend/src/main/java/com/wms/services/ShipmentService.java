package com.wms.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.AssignShipmentUserRequest;
import com.wms.dtos.request.CreateShipmentRequest;
import com.wms.dtos.request.UpdateShipmentStatusRequest;
import com.wms.dtos.request.UpdateShipmentTrackingRequest;
import com.wms.dtos.response.ShipmentAssignmentsResponse;
import com.wms.dtos.response.ShipmentResponse;
import com.wms.dtos.response.UserSummaryResponse;
import com.wms.enums.RoleName;
import com.wms.exceptions.ApiException;
import com.wms.models.Order;
import com.wms.models.Shipment;
import com.wms.models.User;
import com.wms.models.Warehouse;
import com.wms.repositories.OrderRepository;
import com.wms.repositories.RoleRepository;
import com.wms.repositories.ShipmentRepository;
import com.wms.repositories.UserRepository;
import com.wms.repositories.UserRoleRepository;
import com.wms.repositories.WarehouseRepository;

@Service
public class ShipmentService {

    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_PICKING = "PICKING";
    private static final String STATUS_PACKING = "PACKING";
    private static final String STATUS_READY_TO_SHIP = "READY_TO_SHIP";
    private static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_FAILED_DELIVERY = "FAILED_DELIVERY";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final String ORDER_STATUS_PROCESSING = "PROCESSING";
    private static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    private static final String ORDER_STATUS_DELIVERED = "DELIVERED";

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public ShipmentService(
        ShipmentRepository shipmentRepository,
        OrderRepository orderRepository,
        WarehouseRepository warehouseRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        String trackingNumber = normalizeTrackingNumber(request.getTrackingNumber());
        if (shipmentRepository.existsByTrackingNumberIgnoreCase(trackingNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "Tracking number already exists");
        }

        Shipment shipment = new Shipment();
        shipment.setOrder(findOrder(request.getOrderId()));
        shipment.setWarehouse(findWarehouse(request.getWarehouseId()));
        shipment.setShipmentDate(LocalDateTime.now());
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(STATUS_CREATED);
        shipment.setPicker(null);
        shipment.setPacker(null);

        Order order = shipment.getOrder();
        if (!ORDER_STATUS_PROCESSING.equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus()) && !"PENDING".equals(order.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipment cannot be created for order in current status");
        }
        if (!ORDER_STATUS_PROCESSING.equals(order.getStatus())) {
            order.setStatus(ORDER_STATUS_PROCESSING);
            orderRepository.save(order);
        }

        return toResponse(shipmentRepository.save(shipment));
    }

    public List<ShipmentResponse> listShipments() {
        return shipmentRepository.findAllByOrderByShipmentDateDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    public ShipmentResponse getShipment(Long shipmentId) {
        return toResponse(findShipment(shipmentId));
    }

    @Transactional
    public ShipmentResponse updateStatus(Long shipmentId, UpdateShipmentStatusRequest request) {
        Shipment shipment = findShipment(shipmentId);
        String nextStatus = normalizeStatus(request.getStatus());
        validateStatusTransition(shipment.getStatus(), nextStatus);
        shipment.setStatus(nextStatus);
        syncOrderStatus(shipment);
        return toResponse(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentResponse updateTracking(Long shipmentId, UpdateShipmentTrackingRequest request) {
        Shipment shipment = findShipment(shipmentId);
        String trackingNumber = normalizeTrackingNumber(request.getTrackingNumber());
        if (shipmentRepository.existsByTrackingNumberIgnoreCaseAndShipmentIdNot(trackingNumber, shipmentId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Tracking number already exists");
        }

        shipment.setTrackingNumber(trackingNumber);
        return toResponse(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentResponse assignPicker(Long shipmentId, AssignShipmentUserRequest request) {
        Shipment shipment = findShipment(shipmentId);
        shipment.setPicker(resolveAssignedUser(request.getUserId(), RoleName.PICKER, "Picker"));
        if (shipment.getPicker() != null && STATUS_CREATED.equals(shipment.getStatus())) {
            shipment.setStatus(STATUS_PICKING);
        }
        return toResponse(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentResponse assignPacker(Long shipmentId, AssignShipmentUserRequest request) {
        Shipment shipment = findShipment(shipmentId);
        shipment.setPacker(resolveAssignedUser(request.getUserId(), RoleName.PACKER, "Packer"));
        return toResponse(shipmentRepository.save(shipment));
    }

    public ShipmentAssignmentsResponse getAssignments(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);
        return new ShipmentAssignmentsResponse(
            shipment.getShipmentId(),
            toUserSummary(shipment.getPicker()),
            toUserSummary(shipment.getPacker())
        );
    }

    @Transactional
    public ShipmentResponse markPicked(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);
        User currentUser = getCurrentUser();

        if (shipment.getPicker() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipment has no assigned picker");
        }
        if (!shipment.getPicker().getUserId().equals(currentUser.getUserId()) && !hasAnyRole(currentUser, RoleName.ADMIN, RoleName.WAREHOUSE_MANAGER)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only assigned picker can mark shipment picked");
        }
        if (!STATUS_CREATED.equals(shipment.getStatus()) && !STATUS_PICKING.equals(shipment.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipment is not in pickable state");
        }

        shipment.setStatus(STATUS_PACKING);
        return toResponse(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentResponse markPacked(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);
        User currentUser = getCurrentUser();

        if (shipment.getPacker() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipment has no assigned packer");
        }
        if (!shipment.getPacker().getUserId().equals(currentUser.getUserId()) && !hasAnyRole(currentUser, RoleName.ADMIN, RoleName.WAREHOUSE_MANAGER)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only assigned packer can mark shipment packed");
        }
        if (!STATUS_PACKING.equals(shipment.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipment is not in packing state");
        }

        shipment.setStatus(STATUS_READY_TO_SHIP);
        return toResponse(shipmentRepository.save(shipment));
    }

    @Transactional
    public ShipmentResponse dispatch(Long shipmentId) {
        Shipment shipment = findShipment(shipmentId);
        if (!STATUS_READY_TO_SHIP.equals(shipment.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipment is not ready to dispatch");
        }

        shipment.setStatus(STATUS_IN_TRANSIT);
        shipment.getOrder().setStatus(ORDER_STATUS_SHIPPED);
        orderRepository.save(shipment.getOrder());
        return toResponse(shipmentRepository.save(shipment));
    }

    private Shipment findShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shipment not found"));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Warehouse findWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));
    }

    private User resolveAssignedUser(Long userId, String roleName, String label) {
        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, label + " not found"));

        Integer roleId = roleRepository.findByRoleName(roleName)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, roleName + " role not found"))
            .getRoleId();

        if (!userRoleRepository.existsByUserUserIdAndRoleRoleId(userId, roleId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned " + label.toLowerCase() + " must have " + roleName + " role");
        }

        return user;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        String normalized = status.trim().toUpperCase();
        if (!normalized.equals(STATUS_CREATED)
            && !normalized.equals(STATUS_PICKING)
            && !normalized.equals(STATUS_PACKING)
            && !normalized.equals(STATUS_READY_TO_SHIP)
            && !normalized.equals(STATUS_IN_TRANSIT)
            && !normalized.equals(STATUS_DELIVERED)
            && !normalized.equals(STATUS_FAILED_DELIVERY)
            && !normalized.equals(STATUS_CANCELLED)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid shipment status");
        }
        return normalized;
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        if (currentStatus == null || currentStatus.equals(nextStatus)) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case STATUS_CREATED -> nextStatus.equals(STATUS_PICKING) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_PICKING -> nextStatus.equals(STATUS_PACKING) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_PACKING -> nextStatus.equals(STATUS_READY_TO_SHIP) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_READY_TO_SHIP -> nextStatus.equals(STATUS_IN_TRANSIT) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_IN_TRANSIT -> nextStatus.equals(STATUS_DELIVERED) || nextStatus.equals(STATUS_FAILED_DELIVERY);
            case STATUS_FAILED_DELIVERY -> nextStatus.equals(STATUS_IN_TRANSIT) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_DELIVERED, STATUS_CANCELLED -> false;
            default -> false;
        };

        if (!valid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid shipment status transition");
        }
    }

    private String normalizeTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tracking number is required");
        }
        return trackingNumber.trim().toUpperCase();
    }

    private void syncOrderStatus(Shipment shipment) {
        Order order = shipment.getOrder();
        if (STATUS_IN_TRANSIT.equals(shipment.getStatus())) {
            order.setStatus(ORDER_STATUS_SHIPPED);
            orderRepository.save(order);
            return;
        }
        if (STATUS_DELIVERED.equals(shipment.getStatus())) {
            order.setStatus(ORDER_STATUS_DELIVERED);
            orderRepository.save(order);
            return;
        }
        if (STATUS_CANCELLED.equals(shipment.getStatus()) && ORDER_STATUS_SHIPPED.equals(order.getStatus())) {
            order.setStatus(ORDER_STATUS_PROCESSING);
            orderRepository.save(order);
        }
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        User picker = shipment.getPicker();
        User packer = shipment.getPacker();

        return new ShipmentResponse(
            shipment.getShipmentId(),
            shipment.getOrder().getOrderId(),
            shipment.getWarehouse().getWarehouseId(),
            shipment.getWarehouse().getLocation(),
            shipment.getShipmentDate(),
            shipment.getTrackingNumber(),
            shipment.getStatus(),
            picker != null ? picker.getUserId() : null,
            picker != null ? picker.getName() : null,
            packer != null ? packer.getUserId() : null,
            packer != null ? packer.getName() : null
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(user.getUserId(), user.getName(), user.getEmail());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean hasAnyRole(User user, String... roleNames) {
        List<String> currentRoles = userRoleRepository.findByUser(user).stream()
            .map(link -> link.getRole().getRoleName())
            .toList();

        for (String roleName : roleNames) {
            if (currentRoles.contains(roleName)) {
                return true;
            }
        }
        return false;
    }
}
