package com.wms.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.WorkforceTaskResponse;
import com.wms.services.TaskService;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK', 'PICKER', 'PACKER', 'CUSTOMER_SERVICE', 'FINANCE', 'AUDITOR')")
    public ResponseEntity<ApiResponse<List<WorkforceTaskResponse>>> getMyTasks() {
        return ResponseEntity.ok(ApiResponse.ok("Tasks fetched", taskService.getMyTasks()));
    }

    @GetMapping("/picking")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PICKER')")
    public ResponseEntity<ApiResponse<List<WorkforceTaskResponse>>> getPickingTasks() {
        return ResponseEntity.ok(ApiResponse.ok("Picking tasks fetched", taskService.getPickingTasks()));
    }

    @GetMapping("/packing")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PACKER')")
    public ResponseEntity<ApiResponse<List<WorkforceTaskResponse>>> getPackingTasks() {
        return ResponseEntity.ok(ApiResponse.ok("Packing tasks fetched", taskService.getPackingTasks()));
    }

    @GetMapping("/receiving")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'RECEIVING_CLERK')")
    public ResponseEntity<ApiResponse<List<WorkforceTaskResponse>>> getReceivingTasks() {
        return ResponseEntity.ok(ApiResponse.ok("Receiving tasks fetched", taskService.getReceivingTasks()));
    }
}
