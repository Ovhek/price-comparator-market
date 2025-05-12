package com.alexcruceat.pricecomparatormarket.controller.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "API for checking application health status")
public class HealthCheckController {

    @Operation(summary = "Check application status", description = "Returns a simple OK message if the application is running.")
    @ApiResponse(responseCode = "200", description = "Application is healthy")
    @GetMapping
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Application is healthy and running!");
    }
}
