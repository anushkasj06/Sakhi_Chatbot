package com.wms.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserUserId(Long userId);

    boolean existsByEmailIgnoreCase(String email);
}
