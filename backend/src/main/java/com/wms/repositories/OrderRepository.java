package com.wms.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerCustomerIdOrderByOrderDateDesc(Long customerId);

    Optional<Order> findByOrderIdAndCustomerCustomerId(Long orderId, Long customerId);
}
