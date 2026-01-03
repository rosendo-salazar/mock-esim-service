package com.flyroamy.mock.controller;

import com.flyroamy.mock.dto.request.CreateEsimRequest;
import com.flyroamy.mock.dto.response.EsimData;
import com.flyroamy.mock.dto.response.MayaApiResponse;
import com.flyroamy.mock.dto.response.PlanData;
import com.flyroamy.mock.dto.response.ProductData;
import com.flyroamy.mock.model.MockEsim;
import com.flyroamy.mock.model.MockProduct;
import com.flyroamy.mock.service.EsimService;
import com.flyroamy.mock.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/connectivity/v1/esim")
@Tag(name = "eSIMs", description = "Maya Mobile Connect+ eSIM provisioning and management endpoints")
public class EsimController {

    private static final Logger logger = LoggerFactory.getLogger(EsimController.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final EsimService esimService;
    private final ProductService productService;

    public EsimController(EsimService esimService, ProductService productService) {
        this.esimService = esimService;
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create eSIM", description = "Provision a new eSIM with optional plan")
    public ResponseEntity<MayaApiResponse<Void>> createEsim(@Valid @RequestBody CreateEsimRequest request) {
        logger.info("Creating eSIM - planTypeId: {}, region: {}, customerId: {}",
            request.getPlanTypeId(), request.getRegion(), request.getCustomerId());

        MockEsim esim = esimService.createEsim(request);

        MayaApiResponse<Void> response = MayaApiResponse.success(201);
        response.setMessage("eSIM created successfully");
        response.setEsim(mapToEsimData(esim));

        // If plan_type_id is provided, include plan data
        if (request.getPlanTypeId() != null && !request.getPlanTypeId().isEmpty()) {
            if (!esim.getAttachedPlans().isEmpty()) {
                MockEsim.AttachedPlan firstPlan = esim.getAttachedPlans().get(0);
                response.setPlan(mapToPlanData(firstPlan, esim));
            }
        }

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{iccid}")
    @Operation(summary = "Get eSIM", description = "Get eSIM details by ICCID")
    public ResponseEntity<MayaApiResponse<Void>> getEsim(
            @Parameter(description = "ICCID") @PathVariable String iccid) {

        logger.debug("Getting eSIM by ICCID: {}", iccid);

        MockEsim esim = esimService.getEsimByIccid(iccid);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("eSIM retrieved successfully");
        response.setEsim(mapToEsimData(esim));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{iccid}/plan/{planTypeId}")
    @Operation(summary = "Create Plan (Top Up)", description = "Attach a plan to an existing eSIM")
    public ResponseEntity<MayaApiResponse<Void>> createPlan(
            @Parameter(description = "ICCID") @PathVariable String iccid,
            @Parameter(description = "Plan Type ID (Product ID)") @PathVariable String planTypeId) {

        logger.info("Creating plan for eSIM {} with product {}", iccid, planTypeId);

        MockEsim esim = esimService.attachPlan(iccid, planTypeId);

        MayaApiResponse<Void> response = MayaApiResponse.success(201);
        response.setMessage("Plan created successfully");
        response.setIccid(iccid);

        // Find the newly added plan
        if (!esim.getAttachedPlans().isEmpty()) {
            MockEsim.AttachedPlan latestPlan = esim.getAttachedPlans()
                .get(esim.getAttachedPlans().size() - 1);
            response.setPlan(mapToPlanData(latestPlan, esim));
        }

        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{iccid}")
    @Operation(summary = "Delete eSIM", description = "Delete an eSIM by ICCID")
    public ResponseEntity<MayaApiResponse<Void>> deleteEsim(
            @Parameter(description = "ICCID") @PathVariable String iccid) {

        logger.info("Deleting eSIM: {}", iccid);

        esimService.deleteEsim(iccid);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("eSIM deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{iccid}")
    @Operation(summary = "Change eSIM", description = "Update eSIM properties")
    public ResponseEntity<MayaApiResponse<Void>> changeEsim(
            @Parameter(description = "ICCID") @PathVariable String iccid,
            @RequestBody CreateEsimRequest updates) {

        logger.info("Updating eSIM: {}", iccid);

        MockEsim esim = esimService.updateEsim(iccid, updates);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("eSIM updated successfully");
        response.setEsim(mapToEsimData(esim));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{iccid}/plans")
    @Operation(summary = "Get eSIM Plans", description = "Get all plans attached to an eSIM")
    public ResponseEntity<MayaApiResponse<Void>> getEsimPlans(
            @Parameter(description = "ICCID") @PathVariable String iccid) {

        logger.debug("Getting plans for eSIM: {}", iccid);

        MockEsim esim = esimService.getEsimByIccid(iccid);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("Plans retrieved successfully");

        List<PlanData> plans = esim.getAttachedPlans().stream()
            .map(plan -> mapToPlanData(plan, esim))
            .collect(Collectors.toList());

        response.setPlans(plans);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{iccid}/regions")
    @Operation(summary = "Get eSIM Regions", description = "Get all regions covered by eSIM plans")
    public ResponseEntity<MayaApiResponse<Void>> getEsimRegions(
            @Parameter(description = "ICCID") @PathVariable String iccid) {

        logger.debug("Getting regions for eSIM: {}", iccid);

        MockEsim esim = esimService.getEsimByIccid(iccid);

        MayaApiResponse<Void> response = MayaApiResponse.success(200);
        response.setMessage("Regions retrieved successfully");

        // Extract unique regions from all attached plans
        List<String> regions = new ArrayList<>();
        for (MockEsim.AttachedPlan plan : esim.getAttachedPlans()) {
            if (plan.getCountries() != null) {
                regions.addAll(plan.getCountries());
            }
        }
        response.setRegions(regions.stream().distinct().collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    // Helper methods to map domain models to DTOs
    private EsimData mapToEsimData(MockEsim esim) {
        EsimData data = new EsimData();
        data.setUid(esim.getUid());
        data.setIccid(esim.getIccid());
        data.setActivationCode(esim.getActivationCode());
        data.setManualCode(esim.getManualCode());
        data.setSmdpAddress(esim.getSmdpAddress());
        data.setAutoApn(esim.getAutoApn());
        data.setApn(esim.getApn());
        data.setState(esim.getState() != null ? esim.getState() : esim.getStatus());
        data.setServiceStatus(esim.getServiceStatus());
        data.setNetworkStatus(esim.getNetworkStatus());
        data.setCustomerId(esim.getCustomerId());
        data.setTag(esim.getTag());

        if (esim.getDateAssigned() != null) {
            data.setDateAssigned(esim.getDateAssigned().atZone(ZoneOffset.UTC).format(ISO_FORMATTER));
        } else if (esim.getCreatedAt() != null) {
            data.setDateAssigned(esim.getCreatedAt().atZone(ZoneOffset.UTC).format(ISO_FORMATTER));
        }

        return data;
    }

    private PlanData mapToPlanData(MockEsim.AttachedPlan plan, MockEsim esim) {
        PlanData data = new PlanData();
        data.setId(plan.getProductId());
        data.setCountriesEnabled(plan.getCountries());

        if (plan.getDataAllowanceMB() != null) {
            data.setDataQuotaBytes((long) plan.getDataAllowanceMB() * 1024 * 1024);
        }
        if (plan.getRemainingDataMB() != null) {
            data.setDataBytesRemaining((long) plan.getRemainingDataMB() * 1024 * 1024);
        }

        if (plan.getAttachedAt() != null) {
            data.setStartTime(plan.getAttachedAt().atZone(ZoneOffset.UTC).format(ISO_FORMATTER));
        }
        if (plan.getExpiryDate() != null) {
            data.setEndTime(plan.getExpiryDate().atZone(ZoneOffset.UTC).format(ISO_FORMATTER));
        }

        data.setNetworkStatus(esim.getNetworkStatus());

        // Try to fetch and include product data
        try {
            MockProduct product = productService.getProductById(plan.getProductId());
            data.setProduct(mapToProductData(product));
        } catch (Exception e) {
            logger.debug("Could not fetch product details for plan: {}", plan.getProductId());
        }

        return data;
    }

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
        return data;
    }
}
