package com.wms.exceptions;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wms.dtos.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn(
            "api_exception method={} path={} status={} message={} correlationId={}",
            request.getMethod(),
            request.getRequestURI(),
            ex.getStatus(),
            ex.getMessage(),
            MDC.get("correlationId")
        );
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.fail(ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
            .collect(Collectors.toMap(
                error -> ((FieldError) error).getField(),
                error -> error.getDefaultMessage(),
                (first, second) -> first
            ));

        log.warn(
            "validation_exception method={} path={} fieldCount={} correlationId={}",
            request.getMethod(),
            request.getRequestURI(),
            errors.size(),
            MDC.get("correlationId")
        );

        return ResponseEntity.badRequest().body(ApiResponse.fail("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error(
            "unexpected_exception method={} path={} message={} correlationId={}",
            request.getMethod(),
            request.getRequestURI(),
            ex.getMessage(),
            MDC.get("correlationId"),
            ex
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail("Unexpected error occurred", null));
    }
}
