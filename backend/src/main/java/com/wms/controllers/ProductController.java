package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.CreateProductRequest;
import com.wms.dtos.request.UpdateProductPriceRequest;
import com.wms.dtos.request.UpdateProductRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.ProductResponse;
import com.wms.services.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Product created", productService.createProduct(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Products fetched", productService.listProducts()));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok("Product fetched", productService.getProduct(productId)));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
        @PathVariable Long productId,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.updateProduct(productId, request)));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted", null));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'CUSTOMER_SERVICE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(@RequestParam(name = "q", required = false) String query) {
        return ResponseEntity.ok(ApiResponse.ok("Products fetched", productService.searchProducts(query)));
    }

    @PatchMapping("/{productId}/price")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updatePrice(
        @PathVariable Long productId,
        @Valid @RequestBody UpdateProductPriceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product price updated", productService.updatePrice(productId, request)));
    }
}
