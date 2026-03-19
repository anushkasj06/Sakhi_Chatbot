package com.wms.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreateWarehouseRequest;
import com.wms.dtos.request.AssignWarehouseManagerRequest;
import com.wms.dtos.request.UpdateWarehouseRequest;
import com.wms.dtos.response.UserSummaryResponse;
import com.wms.dtos.response.WarehouseResponse;
import com.wms.enums.RoleName;
import com.wms.exceptions.ApiException;
import com.wms.models.User;
import com.wms.models.Warehouse;
import com.wms.repositories.RoleRepository;
import com.wms.repositories.UserRepository;
import com.wms.repositories.UserRoleRepository;
import com.wms.repositories.WarehouseRepository;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public WarehouseService(
        WarehouseRepository warehouseRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (warehouseRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Warehouse email already exists");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setLocation(request.getLocation().trim());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setEmail(email);
        warehouse.setManager(resolveManager(request.getManagerId()));

        return toResponse(warehouseRepository.save(warehouse));
    }

    public List<WarehouseResponse> listWarehouses() {
        return warehouseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public WarehouseResponse getWarehouse(Long warehouseId) {
        return toResponse(findWarehouse(warehouseId));
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long warehouseId, UpdateWarehouseRequest request) {
        Warehouse warehouse = findWarehouse(warehouseId);
        String email = request.getEmail().trim().toLowerCase();

        if (warehouseRepository.existsByEmailIgnoreCaseAndWarehouseIdNot(email, warehouseId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Warehouse email already exists");
        }

        warehouse.setLocation(request.getLocation().trim());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setEmail(email);
        warehouse.setManager(resolveManager(request.getManagerId()));

        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse assignManager(Long warehouseId, AssignWarehouseManagerRequest request) {
        Warehouse warehouse = findWarehouse(warehouseId);
        warehouse.setManager(resolveManager(request.getManagerId()));
        return toResponse(warehouseRepository.save(warehouse));
    }

    public UserSummaryResponse getManager(Long warehouseId) {
        Warehouse warehouse = findWarehouse(warehouseId);
        User manager = warehouse.getManager();
        if (manager == null) {
            return null;
        }

        return new UserSummaryResponse(manager.getUserId(), manager.getName(), manager.getEmail());
    }

    public List<WarehouseResponse> getManagedWarehouses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }

        return warehouseRepository.findByManagerUserId(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void deleteWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found");
        }

        warehouseRepository.deleteById(warehouseId);
    }

    private Warehouse findWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Warehouse not found"));
    }

    private User resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }

        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Manager not found"));

        Integer warehouseManagerRoleId = roleRepository.findByRoleName(RoleName.WAREHOUSE_MANAGER)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WAREHOUSE_MANAGER role not found"))
            .getRoleId();

        if (!userRoleRepository.existsByUserUserIdAndRoleRoleId(managerId, warehouseManagerRoleId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned manager must have WAREHOUSE_MANAGER role");
        }

        return manager;
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        User manager = warehouse.getManager();

        return new WarehouseResponse(
            warehouse.getWarehouseId(),
            warehouse.getLocation(),
            warehouse.getCapacity(),
            warehouse.getEmail(),
            manager != null ? manager.getUserId() : null,
            manager != null ? manager.getName() : null,
            manager != null ? manager.getEmail() : null
        );
    }
}
