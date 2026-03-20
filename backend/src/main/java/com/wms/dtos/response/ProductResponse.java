package com.wms.dtos.response;

import java.math.BigDecimal;

public class ProductResponse {

    private final Long productId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String category;
    private final Long supplierId;
    private final String supplierName;
    private final String supplierEmail;

    public ProductResponse(
        Long productId,
        String name,
        String description,
        BigDecimal price,
        String category,
        Long supplierId,
        String supplierName,
        String supplierEmail
    ) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierEmail = supplierEmail;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }
}
