package com.wms.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreateCustomerOrderRequest;
import com.wms.dtos.request.CustomerOrderItemRequest;
import com.wms.dtos.request.UpdateCustomerProfileRequest;
import com.wms.dtos.response.CustomerOrderItemResponse;
import com.wms.dtos.response.CustomerOrderResponse;
import com.wms.dtos.response.CustomerResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Customer;
import com.wms.models.Order;
import com.wms.models.OrderItem;
import com.wms.models.Product;
import com.wms.models.User;
import com.wms.repositories.CustomerRepository;
import com.wms.repositories.OrderItemRepository;
import com.wms.repositories.OrderRepository;
import com.wms.repositories.ProductRepository;
import com.wms.repositories.UserRepository;

@Service
public class CustomerService {

    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    private static final String ORDER_STATUS_DELIVERED = "DELIVERED";

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CustomerService(
        CustomerRepository customerRepository,
        UserRepository userRepository,
        ProductRepository productRepository,
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository
    ) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public CustomerResponse getMyProfile() {
        return toCustomerResponse(getCurrentCustomer());
    }

    @Transactional
    public CustomerResponse updateMyProfile(UpdateCustomerProfileRequest request) {
        Customer customer = getCurrentCustomer();
        User user = customer.getUser();
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (!customer.getEmail().equalsIgnoreCase(normalizedEmail) && customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "Customer email already exists");
        }

        if (user != null && !user.getEmail().equalsIgnoreCase(normalizedEmail) && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "User email already exists");
        }

        customer.setName(request.getName().trim());
        customer.setAddress(request.getAddress().trim());
        customer.setPhoneNum(request.getPhoneNum().trim());
        customer.setEmail(normalizedEmail);
        customerRepository.save(customer);

        if (user != null) {
            user.setName(request.getName().trim());
            user.setEmail(normalizedEmail);
            userRepository.save(user);
        }

        return toCustomerResponse(customer);
    }

    public CustomerResponse getCustomer(Long customerId) {
        return toCustomerResponse(findCustomer(customerId));
    }

    public List<CustomerResponse> listCustomers() {
        return customerRepository.findAll().stream()
            .map(this::toCustomerResponse)
            .toList();
    }

    @Transactional
    public CustomerOrderResponse createMyOrder(CreateCustomerOrderRequest request) {
        Customer customer = getCurrentCustomer();

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setStatus(ORDER_STATUS_PENDING);
        order.setTotalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CustomerOrderItemRequest itemRequest : request.getItems()) {
            Product product = findProduct(itemRequest.getProductId());
            BigDecimal pricePerUnit = normalizeAmount(product.getPrice());
            BigDecimal lineTotal = pricePerUnit.multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPricePerUnit(pricePerUnit);
            orderItemRepository.save(orderItem);

            totalAmount = totalAmount.add(lineTotal);
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
        return toOrderResponse(order);
    }

    public List<CustomerOrderResponse> listMyOrders() {
        Customer customer = getCurrentCustomer();
        return orderRepository.findByCustomerCustomerIdOrderByOrderDateDesc(customer.getCustomerId()).stream()
            .map(this::toOrderResponse)
            .toList();
    }

    public CustomerOrderResponse getMyOrder(Long orderId) {
        Customer customer = getCurrentCustomer();
        Order order = orderRepository.findByOrderIdAndCustomerCustomerId(orderId, customer.getCustomerId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        return toOrderResponse(order);
    }

    @Transactional
    public CustomerOrderResponse cancelMyOrder(Long orderId) {
        Customer customer = getCurrentCustomer();
        Order order = orderRepository.findByOrderIdAndCustomerCustomerId(orderId, customer.getCustomerId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (ORDER_STATUS_CANCELLED.equalsIgnoreCase(order.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is already cancelled");
        }

        if (ORDER_STATUS_SHIPPED.equalsIgnoreCase(order.getStatus()) || ORDER_STATUS_DELIVERED.equalsIgnoreCase(order.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order cannot be cancelled in current status");
        }

        order.setStatus(ORDER_STATUS_CANCELLED);
        orderRepository.save(order);
        return toOrderResponse(order);
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return customerRepository.findByUserUserId(user.getUserId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer profile not found"));
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
            customer.getCustomerId(),
            customer.getName(),
            customer.getAddress(),
            customer.getPhoneNum(),
            customer.getEmail(),
            customer.getUser() != null ? customer.getUser().getUserId() : null
        );
    }

    private CustomerOrderResponse toOrderResponse(Order order) {
        List<CustomerOrderItemResponse> items = orderItemRepository.findByOrderOrderId(order.getOrderId()).stream()
            .map(this::toOrderItemResponse)
            .toList();

        return new CustomerOrderResponse(
            order.getOrderId(),
            order.getCustomer().getCustomerId(),
            order.getOrderDate(),
            order.getDeliveryDate(),
            normalizeAmount(order.getTotalAmount()),
            order.getStatus(),
            items
        );
    }

    private CustomerOrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        BigDecimal lineTotal = orderItem.getPricePerUnit()
            .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
            .setScale(2, RoundingMode.HALF_UP);

        return new CustomerOrderItemResponse(
            orderItem.getOrderItemId(),
            orderItem.getProduct().getProductId(),
            orderItem.getProduct().getName(),
            orderItem.getQuantity(),
            normalizeAmount(orderItem.getPricePerUnit()),
            lineTotal
        );
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
