package com.wms.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreateSupplierRequest;
import com.wms.dtos.request.UpdateSupplierRequest;
import com.wms.dtos.response.ProductResponse;
import com.wms.dtos.response.SupplierResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Product;
import com.wms.models.Supplier;
import com.wms.repositories.ProductRepository;
import com.wms.repositories.SupplierRepository;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public SupplierService(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (supplierRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Supplier email already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setSName(request.getSName().trim());
        supplier.setContactPerson(request.getContactPerson().trim());
        supplier.setEmail(email);

        return toResponse(supplierRepository.save(supplier));
    }

    public List<SupplierResponse> listSuppliers() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SupplierResponse getSupplier(Long supplierId) {
        return toResponse(findSupplier(supplierId));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long supplierId, UpdateSupplierRequest request) {
        Supplier supplier = findSupplier(supplierId);
        String email = request.getEmail().trim().toLowerCase();

        if (supplierRepository.existsByEmailIgnoreCaseAndSupplierIdNot(email, supplierId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Supplier email already exists");
        }

        supplier.setSName(request.getSName().trim());
        supplier.setContactPerson(request.getContactPerson().trim());
        supplier.setEmail(email);

        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Supplier not found");
        }

        if (productRepository.existsBySupplierSupplierId(supplierId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete supplier with mapped products");
        }

        supplierRepository.deleteById(supplierId);
    }

    public List<ProductResponse> getSupplierProducts(Long supplierId) {
        Supplier supplier = findSupplier(supplierId);
        return productRepository.findBySupplierSupplierId(supplier.getSupplierId())
            .stream()
            .map(this::toProductResponse)
            .toList();
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
            supplier.getSupplierId(),
            supplier.getSName(),
            supplier.getContactPerson(),
            supplier.getEmail()
        );
    }

    private ProductResponse toProductResponse(Product product) {
        Supplier supplier = product.getSupplier();
        return new ProductResponse(
            product.getProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            supplier.getSupplierId(),
            supplier.getSName(),
            supplier.getEmail()
        );
    }
}
