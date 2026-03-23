package com.wms.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreateOrderItemRequest;
import com.wms.dtos.request.CreateOrderRequest;
import com.wms.dtos.request.UpdateOrderItemRequest;
import com.wms.dtos.request.UpdateOrderRequest;
import com.wms.dtos.request.UpdateOrderStatusRequest;
import com.wms.dtos.response.OrderItemResponse;
import com.wms.dtos.response.OrderResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Customer;
import com.wms.models.Order;
import com.wms.models.OrderItem;
import com.wms.models.Product;
import com.wms.repositories.CustomerRepository;
import com.wms.repositories.OrderItemRepository;
import com.wms.repositories.OrderRepository;
import com.wms.repositories.ProductRepository;

@Service
public class OrderService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        CustomerRepository customerRepository,
        ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomer(findCustomer(request.getCustomerId()));
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setTotalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        order.setStatus(STATUS_PENDING);
        return toResponse(orderRepository.save(order));
    }

    public List<OrderResponse> listOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    public OrderResponse getOrder(Long orderId) {
        return toResponse(findOrder(orderId));
    }

    @Transactional
    public OrderResponse updateOrder(Long orderId, UpdateOrderRequest request) {
        Order order = findOrder(orderId);
        ensureOrderEditable(order);

        String nextStatus = normalizeStatus(request.getStatus());
        validateStatusTransition(order.getStatus(), nextStatus);

        order.setCustomer(findCustomer(request.getCustomerId()));
        order.setDeliveryDate(request.getDeliveryDate());
        order.setStatus(nextStatus);

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findOrder(orderId);
        String nextStatus = normalizeStatus(request.getStatus());
        validateStatusTransition(order.getStatus(), nextStatus);
        order.setStatus(nextStatus);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrder(orderId);
        if (STATUS_CANCELLED.equals(order.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is already cancelled");
        }
        if (STATUS_SHIPPED.equals(order.getStatus()) || STATUS_DELIVERED.equals(order.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order cannot be cancelled in current status");
        }
        order.setStatus(STATUS_CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse addItem(Long orderId, CreateOrderItemRequest request) {
        Order order = findOrder(orderId);
        ensureOrderEditable(order);

        OrderItem orderItem = new OrderItem();
        Product product = findProduct(request.getProductId());
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(request.getQuantity());
        orderItem.setPricePerUnit(normalizeAmount(product.getPrice()));
        orderItemRepository.save(orderItem);

        recalculateTotal(order);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateItem(Long orderId, Long orderItemId, UpdateOrderItemRequest request) {
        Order order = findOrder(orderId);
        ensureOrderEditable(order);

        OrderItem orderItem = orderItemRepository.findByOrderItemIdAndOrderOrderId(orderItemId, orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));

        Product product = findProduct(request.getProductId());
        orderItem.setProduct(product);
        orderItem.setQuantity(request.getQuantity());
        orderItem.setPricePerUnit(normalizeAmount(product.getPrice()));
        orderItemRepository.save(orderItem);

        recalculateTotal(order);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse deleteItem(Long orderId, Long orderItemId) {
        Order order = findOrder(orderId);
        ensureOrderEditable(order);

        OrderItem orderItem = orderItemRepository.findByOrderItemIdAndOrderOrderId(orderItemId, orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));

        orderItemRepository.delete(orderItem);
        recalculateTotal(order);
        return toResponse(orderRepository.save(order));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void ensureOrderEditable(Order order) {
        if (STATUS_CANCELLED.equals(order.getStatus())
            || STATUS_SHIPPED.equals(order.getStatus())
            || STATUS_DELIVERED.equals(order.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Finalized order cannot be modified");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        String normalized = status.trim().toUpperCase();
        if (!normalized.equals(STATUS_PENDING)
            && !normalized.equals(STATUS_CONFIRMED)
            && !normalized.equals(STATUS_PROCESSING)
            && !normalized.equals(STATUS_SHIPPED)
            && !normalized.equals(STATUS_DELIVERED)
            && !normalized.equals(STATUS_CANCELLED)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid order status");
        }

        return normalized;
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        if (currentStatus == null || currentStatus.equals(nextStatus)) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case STATUS_PENDING -> nextStatus.equals(STATUS_CONFIRMED) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_CONFIRMED -> nextStatus.equals(STATUS_PROCESSING) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_PROCESSING -> nextStatus.equals(STATUS_SHIPPED) || nextStatus.equals(STATUS_CANCELLED);
            case STATUS_SHIPPED -> nextStatus.equals(STATUS_DELIVERED);
            case STATUS_DELIVERED, STATUS_CANCELLED -> false;
            default -> false;
        };

        if (!valid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid order status transition");
        }
    }

    private void recalculateTotal(Order order) {
        BigDecimal total = orderItemRepository.findByOrderOrderId(order.getOrderId()).stream()
            .map(item -> normalizeAmount(item.getPricePerUnit()).multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        order.setTotalAmount(total);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderOrderId(order.getOrderId()).stream()
            .map(this::toItemResponse)
            .toList();

        return new OrderResponse(
            order.getOrderId(),
            order.getCustomer().getCustomerId(),
            order.getCustomer().getName(),
            order.getCustomer().getEmail(),
            order.getOrderDate(),
            order.getDeliveryDate(),
            normalizeAmount(order.getTotalAmount()),
            order.getStatus(),
            items
        );
    }

    private OrderItemResponse toItemResponse(OrderItem orderItem) {
        BigDecimal pricePerUnit = normalizeAmount(orderItem.getPricePerUnit());
        BigDecimal lineTotal = pricePerUnit.multiply(BigDecimal.valueOf(orderItem.getQuantity()))
            .setScale(2, RoundingMode.HALF_UP);

        return new OrderItemResponse(
            orderItem.getOrderItemId(),
            orderItem.getProduct().getProductId(),
            orderItem.getProduct().getName(),
            orderItem.getQuantity(),
            pricePerUnit,
            lineTotal
        );
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be a valid non-negative number");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
