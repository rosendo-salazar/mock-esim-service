package com.flyroamy.mock.controller;

import com.flyroamy.mock.model.MockBundle;
import com.flyroamy.mock.service.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/connectivity/bundles")
@Tag(name = "Bundles", description = "Bundle/Plan management endpoints")
public class BundleController {

    private static final Logger logger = LoggerFactory.getLogger(BundleController.class);

    private final BundleService bundleService;

    public BundleController(BundleService bundleService) {
        this.bundleService = bundleService;
    }

    @GetMapping
    @Operation(summary = "List all bundles", description = "Get all available bundles with optional filtering")
    public ResponseEntity<Map<String, Object>> listBundles(
            @Parameter(description = "Filter by country ISO code") @RequestParam(required = false) String country,
            @Parameter(description = "Filter by region") @RequestParam(required = false) String region,
            @Parameter(description = "Filter by package type") @RequestParam(required = false) String packageType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "100") int size) {

        logger.debug("Listing bundles - country: {}, region: {}, packageType: {}, page: {}, size: {}",
            country, region, packageType, page, size);

        Page<MockBundle> bundlePage;

        if (country != null && !country.isEmpty()) {
            bundlePage = bundleService.getBundlesByCountry(country, page, size);
        } else if (region != null && !region.isEmpty()) {
            bundlePage = bundleService.getBundlesByRegion(region, page, size);
        } else if (packageType != null && !packageType.isEmpty()) {
            bundlePage = bundleService.getBundlesByPackageType(packageType, page, size);
        } else {
            bundlePage = bundleService.getAllBundles(page, size);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("bundles", bundlePage.getContent().stream()
            .map(this::mapBundleToResponse)
            .toList());
        response.put("pagination", Map.of(
            "page", bundlePage.getNumber(),
            "size", bundlePage.getSize(),
            "totalElements", bundlePage.getTotalElements(),
            "totalPages", bundlePage.getTotalPages()
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bundleId}")
    @Operation(summary = "Get bundle details", description = "Get details of a specific bundle")
    public ResponseEntity<Map<String, Object>> getBundle(
            @Parameter(description = "Bundle ID") @PathVariable String bundleId) {

        logger.debug("Getting bundle: {}", bundleId);

        MockBundle bundle = bundleService.getBundleById(bundleId);
        return ResponseEntity.ok(mapBundleToResponse(bundle));
    }

    @PostMapping
    @Operation(summary = "Create bundle", description = "Create a new bundle (admin)")
    public ResponseEntity<Map<String, Object>> createBundle(@RequestBody MockBundle bundle) {
        logger.info("Creating bundle: {}", bundle.getName());

        MockBundle created = bundleService.createBundle(bundle);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Bundle created successfully");
        response.put("bundle", mapBundleToResponse(created));

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{bundleId}")
    @Operation(summary = "Update bundle", description = "Update an existing bundle (admin)")
    public ResponseEntity<Map<String, Object>> updateBundle(
            @PathVariable String bundleId,
            @RequestBody MockBundle updates) {

        logger.info("Updating bundle: {}", bundleId);

        MockBundle updated = bundleService.updateBundle(bundleId, updates);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Bundle updated successfully");
        response.put("bundle", mapBundleToResponse(updated));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bundleId}")
    @Operation(summary = "Delete bundle", description = "Delete a bundle (admin)")
    public ResponseEntity<Map<String, Object>> deleteBundle(@PathVariable String bundleId) {
        logger.info("Deleting bundle: {}", bundleId);

        bundleService.deleteBundle(bundleId);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Bundle deleted successfully",
            "bundleId", bundleId
        ));
    }

    private Map<String, Object> mapBundleToResponse(MockBundle bundle) {
        Map<String, Object> response = new HashMap<>();
        response.put("bundleId", bundle.getBundleId());
        response.put("productId", bundle.getProductId());
        response.put("name", bundle.getName());
        response.put("description", bundle.getDescription());
        response.put("dataGB", bundle.getDataGB());
        response.put("validityDays", bundle.getValidityDays());
        response.put("price", Map.of(
            "amount", bundle.getPrice() != null ? bundle.getPrice() : 0,
            "currency", bundle.getCurrency() != null ? bundle.getCurrency() : "USD"
        ));
        response.put("prices", bundle.getPrices());
        response.put("wholesaleCost", bundle.getWholesaleCost());
        response.put("packageType", bundle.getPackageType());
        response.put("countries", bundle.getCountries());
        response.put("region", bundle.getRegion());
        response.put("coverage", bundle.getCoverage());
        response.put("isActive", bundle.isActive());
        response.put("badge", bundle.getBadge());
        response.put("unlimitedType", bundle.getUnlimitedType());
        response.put("terms", bundle.getTerms());
        response.put("createdAt", bundle.getCreatedAt());
        response.put("updatedAt", bundle.getUpdatedAt());
        return response;
    }
}
