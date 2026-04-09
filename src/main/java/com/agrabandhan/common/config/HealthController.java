package com.agrabandhan.common.config;

import com.agrabandhan.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(name = "Health", description = "Application health check")
public class HealthController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = Map.of(
                "application", appName,
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "version", "1.0.0"
        );
        return ResponseEntity.ok(ApiResponse.success(healthData));
    }
}
