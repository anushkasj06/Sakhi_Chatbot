package com.wms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wms.models.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
