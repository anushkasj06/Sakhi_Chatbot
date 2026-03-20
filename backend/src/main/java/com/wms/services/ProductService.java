package com.wms.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreateProductRequest;
import com.wms.dtos.request.UpdateProductPriceRequest;
import com.wms.dtos.request.UpdateProductRequest;
import com.wms.dtos.response.ProductResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Product;
import com.wms.models.Supplier;
import com.wms.repositories.ProductRepository;
import com.wms.repositories.SupplierRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Supplier supplier = findSupplier(request.getSupplierId());

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        product.setPrice(normalizePrice(request.getPrice()));
        product.setCategory(request.getCategory().trim());
        product.setSupplier(supplier);

        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse getProduct(Long productId) {
        return toResponse(findProduct(productId));
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        Product product = findProduct(productId);
        Supplier supplier = findSupplier(request.getSupplierId());

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        product.setPrice(normalizePrice(request.getPrice()));
        product.setCategory(request.getCategory().trim());
        product.setSupplier(supplier);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updatePrice(Long productId, UpdateProductPriceRequest request) {
        Product product = findProduct(productId);
        product.setPrice(normalizePrice(request.getPrice()));
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(productId);
    }

    public List<ProductResponse> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return listProducts();
        }

        String normalized = query.trim();
        return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(normalized, normalized)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    private BigDecimal normalizePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Price must be greater than zero");
        }
        return price.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private ProductResponse toResponse(Product product) {
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
