package com.wms.controllers;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wms.dtos.response.ApiResponse;
import com.wms.dtos.response.HealthStatusResponse;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/live")
    public ResponseEntity<ApiResponse<HealthStatusResponse>> live() {
        return ResponseEntity.ok(ApiResponse.ok(
            "Service is live",
            new HealthStatusResponse("UP", "wms-backend", LocalDateTime.now(), "Application is running")
        ));
    }

    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<HealthStatusResponse>> ready() {
        Integer dbPing = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        String status = dbPing != null && dbPing == 1 ? "UP" : "DOWN";
        String details = "UP".equals(status) ? "Application and database are ready" : "Database readiness check failed";

        return ResponseEntity.ok(ApiResponse.ok(
            "Readiness checked",
            new HealthStatusResponse(status, "wms-backend", LocalDateTime.now(), details)
        ));
    }
}
