package com.flyroamy.mock.controller;

import com.flyroamy.mock.dto.request.ForceStatusRequest;
import com.flyroamy.mock.dto.request.SimulateUsageRequest;
import com.flyroamy.mock.model.MockEsim;
import com.flyroamy.mock.service.DataSeederService;
import com.flyroamy.mock.service.EsimService;
import com.flyroamy.mock.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin", description = "Administrative and testing endpoints")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final EsimService esimService;
    private final ProductService productService;
    private final DataSeederService dataSeederService;

    public AdminController(EsimService esimService, ProductService productService, DataSeederService dataSeederService) {
        this.esimService = esimService;
        this.productService = productService;
        this.dataSeederService = dataSeederService;
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is healthy (public endpoint)")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "mock-esim-service",
            "timestamp", Instant.now().toString(),
            "statistics", Map.of(
                "products", productService.getProductCount(),
                "esims", esimService.getStatistics()
            )
        ));
    }

    @PostMapping("/simulate/usage")
    @Operation(summary = "Simulate data usage", description = "Simulate data usage on an eSIM for testing")
    public ResponseEntity<Map<String, Object>> simulateUsage(@Valid @RequestBody SimulateUsageRequest request) {
        logger.info("Simulating {} MB usage on eSIM: {}", request.getUsageMB(), request.getEsimId());

        MockEsim esim = esimService.simulateUsage(request.getEsimId(), request.getUsageMB());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Usage simulated successfully",
            "esimId", esim.getEsimId(),
            "usageSimulatedMB", request.getUsageMB(),
            "totalDataUsedMB", esim.getTotalDataUsedMB(),
            "totalRemainingDataMB", esim.getTotalRemainingDataMB()
        ));
    }

    @PostMapping("/simulate/status")
    @Operation(summary = "Force status change", description = "Force a status change on an eSIM for testing")
    public ResponseEntity<Map<String, Object>> forceStatus(@Valid @RequestBody ForceStatusRequest request) {
        logger.info("Forcing status {} on eSIM: {}", request.getNewStatus(), request.getEsimId());

        MockEsim esim = esimService.forceStatusChange(request.getEsimId(), request.getNewStatus());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Status changed successfully",
            "esimId", esim.getEsimId(),
            "newStatus", esim.getStatus()
        ));
    }

    @DeleteMapping("/reset")
    @Operation(summary = "Reset all data", description = "Delete all eSIMs and products, reset to initial state")
    public ResponseEntity<Map<String, Object>> resetData() {
        logger.warn("Resetting all data");

        esimService.deleteAll();
        productService.deleteAll();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "All data has been reset"
        ));
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed test data", description = "Seed the database with test products")
    public ResponseEntity<Map<String, Object>> seedData() {
        logger.info("Seeding test data");

        int productsCreated = dataSeederService.seedProducts();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Test data seeded successfully",
            "productsCreated", productsCreated
        ));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get statistics", description = "Get service statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(Map.of(
            "products", Map.of(
                "total", productService.getProductCount()
            ),
            "esims", esimService.getStatistics()
        ));
    }
}
