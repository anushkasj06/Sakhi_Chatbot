package com.wms.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.ConfirmPaymentRequest;
import com.wms.dtos.request.CreatePaymentIntentRequest;
import com.wms.dtos.request.RefundPaymentRequest;
import com.wms.dtos.response.PaymentResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Customer;
import com.wms.models.Order;
import com.wms.models.Payment;
import com.wms.models.User;
import com.wms.repositories.CustomerRepository;
import com.wms.repositories.OrderRepository;
import com.wms.repositories.PaymentRepository;
import com.wms.repositories.UserRepository;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private static final String STATUS_INITIATED = "INITIATED";
    private static final String STATUS_AUTHORIZED = "AUTHORIZED";
    private static final String STATUS_CAPTURED = "CAPTURED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_REFUNDED = "REFUNDED";
    private static final String STATUS_PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";

    private static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    private static final String ORDER_STATUS_PROCESSING = "PROCESSING";

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public PaymentService(
        PaymentRepository paymentRepository,
        OrderRepository orderRepository,
        UserRepository userRepository,
        CustomerRepository customerRepository,
        AuditService auditService
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentResponse createIntent(CreatePaymentIntentRequest request) {
        Order order = findOrder(request.getOrderId());
        ensureCustomerCanAccessOrder(order);

        BigDecimal amount = normalizeAmount(request.getAmount());
        if (amount.compareTo(normalizeAmount(order.getTotalAmount())) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment amount cannot exceed order total amount");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTransactionId(generateTransactionId("INTENT"));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(amount);
        payment.setPaymentMethod(request.getPaymentMethod().trim().toUpperCase(Locale.ROOT));
        payment.setStatus(STATUS_INITIATED);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment intent created: paymentId={}, orderId={}, amount={}", saved.getPaymentId(), saved.getOrder().getOrderId(), saved.getAmount());
        auditService.logEvent("PAYMENT", "PAYMENT", String.valueOf(saved.getPaymentId()), "INTENT", "Payment intent created");
        return toResponse(saved);
    }

    @Transactional
    public PaymentResponse confirm(ConfirmPaymentRequest request) {
        Payment payment = findPayment(request.getPaymentId());
        ensureCustomerCanAccessOrder(payment.getOrder());

        String transactionId = request.getTransactionId().trim();
        if (!payment.getTransactionId().equals(transactionId) && paymentRepository.existsByTransactionId(transactionId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Transaction id already exists");
        }

        String status = normalizeConfirmStatus(request.getStatus());
        payment.setTransactionId(transactionId);
        payment.setStatus(status);
        payment.setPaymentDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        log.info("Payment confirmed: paymentId={}, orderId={}, status={}", saved.getPaymentId(), saved.getOrder().getOrderId(), saved.getStatus());

        if (STATUS_AUTHORIZED.equals(status) || STATUS_CAPTURED.equals(status)) {
            Order order = saved.getOrder();
            if (!ORDER_STATUS_PROCESSING.equals(order.getStatus())) {
                order.setStatus(ORDER_STATUS_CONFIRMED);
                orderRepository.save(order);
            }
        }

        auditService.logEvent("PAYMENT", "PAYMENT", String.valueOf(saved.getPaymentId()), "CONFIRM", "Payment confirmed with status " + saved.getStatus());

        return toResponse(saved);
    }

    public PaymentResponse getById(Long paymentId) {
        Payment payment = findPayment(paymentId);
        ensureCustomerCanAccessOrder(payment.getOrder());
        return toResponse(payment);
    }

    public List<PaymentResponse> getByOrderId(Long orderId) {
        Order order = findOrder(orderId);
        ensureCustomerCanAccessOrder(order);

        return paymentRepository.findByOrderOrderIdOrderByPaymentDateDesc(orderId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public PaymentResponse refund(Long paymentId, RefundPaymentRequest request) {
        Payment payment = findPayment(paymentId);

        if (!STATUS_CAPTURED.equals(payment.getStatus()) && !STATUS_AUTHORIZED.equals(payment.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only CAPTURED or AUTHORIZED payments can be refunded");
        }

        BigDecimal refundAmount = normalizeAmount(request.getAmount());
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Refund amount cannot exceed paid amount");
        }

        payment.setStatus(refundAmount.compareTo(payment.getAmount()) == 0 ? STATUS_REFUNDED : STATUS_PARTIALLY_REFUNDED);
        payment.setPaymentDate(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        log.info("Payment refund processed: paymentId={}, status={}, amount={}", saved.getPaymentId(), saved.getStatus(), request.getAmount());
        auditService.logEvent("PAYMENT", "PAYMENT", String.valueOf(saved.getPaymentId()), "REFUND", "Refund processed with status " + saved.getStatus());
        return toResponse(saved);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeConfirmStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS_AUTHORIZED.equals(normalized)
            && !STATUS_CAPTURED.equals(normalized)
            && !STATUS_FAILED.equals(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Confirm status must be AUTHORIZED, CAPTURED, or FAILED");
        }

        return normalized;
    }

    private String generateTransactionId(String prefix) {
        String candidate = prefix + "-" + UUID.randomUUID();
        while (paymentRepository.existsByTransactionId(candidate)) {
            candidate = prefix + "-" + UUID.randomUUID();
        }
        return candidate;
    }

    private void ensureCustomerCanAccessOrder(Order order) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isCustomerRole = authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()));

        if (!isCustomerRole) {
            return;
        }

        Customer customer = customerRepository.findByUserUserId(user.getUserId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer profile not found"));

        if (!order.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access payments for this order");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getPaymentId(),
            payment.getOrder().getOrderId(),
            payment.getTransactionId(),
            payment.getPaymentDate(),
            payment.getAmount(),
            payment.getPaymentMethod(),
            payment.getStatus()
        );
    }
}
