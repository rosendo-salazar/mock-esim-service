package com.flyroamy.mock.controller;

import com.flyroamy.mock.dto.request.BulkProductUploadRequest;
import com.flyroamy.mock.dto.response.BalanceData;
import com.flyroamy.mock.dto.response.MayaApiResponse;
import com.flyroamy.mock.dto.response.ProductData;
import com.flyroamy.mock.model.MockProduct;
import com.flyroamy.mock.service.ProductBulkUploadService;
import com.flyroamy.mock.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/connectivity/v1/account")
@Tag(name = "Products", description = "Maya Mobile Connect+ Product/Plan management endpoints")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ProductBulkUploadService bulkUploadService;

    public ProductController(ProductService productService, ProductBulkUploadService bulkUploadService) {
        this.productService = productService;
        this.bulkUploadService = bulkUploadService;
    }

    @GetMapping("/products")
    @Operation(summary = "Get All Products", description = "Get all available products with optional filtering")
    public ResponseEntity<MayaApiResponse<Void>> getAllProducts(
            @Parameter(description = "Filter by country ISO2 code") @RequestParam(required = false) String country,
            @Parameter(description = "Filter by region (europe, apac, latam, etc.)") @RequestParam(required = false) String region,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "100") int size) {

        logger.debug("Listing products - country: {}, region: {}, page: {}, size: {}",
            country, region, page, size);

        Page<MockProduct> productPage;

        if (country != null && !country.isEmpty()) {
            productPage = productService.getProductsByCountry(country, page, size);
        } else if (region != null && !region.isEmpty()) {
            productPage = productService.getProductsByRegion(region, page, size);
        } else {
            productPage = productService.getAllProducts(page, size);
        }

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("Products retrieved successfully");

        List<ProductData> products = productPage.getContent().stream()
            .map(this::mapToProductData)
            .collect(Collectors.toList());

        response.setProducts(products);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get Product", description = "Get details of a specific product")
    public ResponseEntity<MayaApiResponse<Void>> getProduct(
            @Parameter(description = "Product ID") @PathVariable String productId) {

        logger.debug("Getting product: {}", productId);

        MockProduct product = productService.getProductById(productId);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("Product retrieved successfully");
        response.setProduct(mapToProductData(product));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    @Operation(summary = "Get Account Balance", description = "Get account balance information")
    public ResponseEntity<MayaApiResponse<Void>> getAccountBalance() {

        logger.debug("Getting account balance");

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("Balance retrieved successfully");

        // Mock balance data
        BalanceData balance = new BalanceData();
        balance.setBalance(1000.00);
        balance.setCurrency("USD");
        response.setBalance(balance);

        return ResponseEntity.ok(response);
    }

    // Admin endpoints for product management (keeping for backward compatibility and testing)
    @PostMapping("/products")
    @Operation(summary = "Create product", description = "Create a new product (admin)")
    public ResponseEntity<MayaApiResponse<Void>> createProduct(@RequestBody MockProduct product) {
        logger.info("Creating product: {}", product.getName());

        MockProduct created = productService.createProduct(product);

        MayaApiResponse<Void> response = MayaApiResponse.success(201);
        response.setMessage("Product created successfully");
        response.setProduct(mapToProductData(created));

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/products/{productId}")
    @Operation(summary = "Update product", description = "Update an existing product (admin)")
    public ResponseEntity<MayaApiResponse<Void>> updateProduct(
            @PathVariable String productId,
            @RequestBody MockProduct updates) {

        logger.info("Updating product: {}", productId);

        MockProduct updated = productService.updateProduct(productId, updates);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("Product updated successfully");
        response.setProduct(mapToProductData(updated));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{productId}")
    @Operation(summary = "Delete product", description = "Delete a product (admin)")
    public ResponseEntity<MayaApiResponse<Void>> deleteProduct(@PathVariable String productId) {
        logger.info("Deleting product: {}", productId);

        productService.deleteProduct(productId);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("Product deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/bulk-upload")
    @Operation(summary = "Bulk upload products", description = "Bulk upload products from Excel data (admin)")
    public ResponseEntity<Map<String, Object>> bulkUploadProducts(@RequestBody BulkProductUploadRequest request) {
        logger.info("Bulk uploading {} products",
            request.getProducts() != null ? request.getProducts().size() : 0);

        ProductBulkUploadService.BulkUploadResult result = bulkUploadService.processBulkUpload(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.isSuccess() ? "Products uploaded successfully" : "Upload completed with errors");
        response.put("created", result.getCreated());
        response.put("updated", result.getUpdated());
        response.put("total", result.getTotal());
        response.put("errors", result.getErrors());

        return ResponseEntity.ok(response);
    }

    // Helper method to map domain model to DTO
    private ProductData mapToProductData(MockProduct product) {
        ProductData data = new ProductData();
        data.setUid(product.getUid() != null ? product.getUid() : product.getProductId());
        data.setName(product.getName());
        data.setCountriesEnabled(product.getCountriesEnabled() != null ?
            product.getCountriesEnabled() : product.getCountries());
        data.setDataQuotaMb(product.getDataQuotaMb());
        data.setDataQuotaBytes(product.getDataQuotaBytes());
        data.setValidityDays(product.getValidityDays());
        data.setPolicyId(product.getPolicyId());
        data.setPolicyName(product.getPolicyName());
        data.setWholesalePriceUsd(product.getWholesalePriceUsd());
        data.setRrpUsd(product.getRrpUsd());
        data.setRrpEur(product.getRrpEur());
        data.setRrpGbp(product.getRrpGbp());
        data.setRrpCad(product.getRrpCad());
        data.setRrpAud(product.getRrpAud());
        data.setRrpJpy(product.getRrpJpy());
        data.setUnlimitedType(product.getUnlimitedType());
        return data;
    }
}
