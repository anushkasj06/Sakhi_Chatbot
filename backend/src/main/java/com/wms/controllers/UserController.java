package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.request.AssignRoleRequest;
import com.wms.dtos.request.CreateUserRequest;
import com.wms.dtos.request.UpdateUserRequest;
import com.wms.dtos.request.UpdateUserStatusRequest;
import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.UserResponse;
import com.wms.dtos.response.WarehouseResponse;
import com.wms.services.UserService;
import com.wms.services.WarehouseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final WarehouseService warehouseService;

    public UserController(UserService userService, WarehouseService warehouseService) {
        this.userService = userService;
        this.warehouseService = warehouseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User created", userService.createUser(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.listUsers()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched", userService.getUser(userId)));
    }

    @GetMapping("/{userId}/managed-warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getManagedWarehouses(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Managed warehouses fetched", warehouseService.getManagedWarehouses(userId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userService.updateUser(userId, request)));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long userId, @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User status updated", userService.updateStatus(userId, request)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(@PathVariable Long userId, @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Role assigned", userService.assignRole(userId, request.getRoleName())));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<UserResponse>> removeRole(@PathVariable Long userId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(ApiResponse.ok("Role removed", userService.removeRole(userId, roleId)));
    }
}
