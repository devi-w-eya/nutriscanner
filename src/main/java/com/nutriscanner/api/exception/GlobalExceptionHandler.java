package com.nutriscanner.api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex) {

        int status = switch (ex.getMessage()) {
            case "EMAIL_ALREADY_EXISTS" -> 409;
            case "INVALID_CREDENTIALS"  -> 401;
            case "ACCOUNT_DISABLED"     -> 403;
            case "PRODUCT_NOT_FOUND"    -> 404;
            case "INVALID_BARCODE"      -> 400;
            case "OFF_UNAVAILABLE"      -> 503;
            case "AI_UNAVAILABLE"       -> 503;
            default                     -> 500;
        };

        return ResponseEntity.status(status).body(Map.of(
                "status", status,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
