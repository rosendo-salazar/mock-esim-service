package com.flyroamy.mock.controller;

import com.flyroamy.mock.dto.request.AttachBundleRequest;
import com.flyroamy.mock.dto.request.ProvisionEsimRequest;
import com.flyroamy.mock.model.MockEsim;
import com.flyroamy.mock.service.EsimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/connectivity/esims")
@Tag(name = "eSIMs", description = "eSIM provisioning and management endpoints")
public class EsimController {

    private static final Logger logger = LoggerFactory.getLogger(EsimController.class);

    private final EsimService esimService;

    public EsimController(EsimService esimService) {
        this.esimService = esimService;
    }

    @PostMapping
    @Operation(summary = "Provision eSIM", description = "Provision a new eSIM with an initial bundle")
    public ResponseEntity<Map<String, Object>> provisionEsim(@Valid @RequestBody ProvisionEsimRequest request) {
        logger.info("Provisioning eSIM for bundle: {}", request.getBundleId());

        MockEsim esim = esimService.provisionEsim(request);

        Map<String, Object> response = new HashMap<>();
        response.put("esimId", esim.getEsimId());
        response.put("iccid", esim.getIccid());
        response.put("matchingId", esim.getMatchingId());
        response.put("qrCodeUrl", esim.getQrCodeUrl());
        response.put("qrCodeData", esim.getQrCodeData());
        response.put("activationCode", esim.getActivationCode());
        response.put("status", esim.getStatus());
        response.put("createdAt", esim.getCreatedAt());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{esimId}")
    @Operation(summary = "Get eSIM details", description = "Get details of a specific eSIM including all attached bundles")
    public ResponseEntity<Map<String, Object>> getEsim(
            @Parameter(description = "eSIM ID") @PathVariable String esimId) {

        logger.debug("Getting eSIM: {}", esimId);

        MockEsim esim = esimService.getEsimById(esimId);
        return ResponseEntity.ok(mapEsimToDetailResponse(esim));
    }

    @GetMapping("/iccid/{iccid}")
    @Operation(summary = "Get eSIM by ICCID", description = "Get eSIM details by ICCID")
    public ResponseEntity<Map<String, Object>> getEsimByIccid(
            @Parameter(description = "ICCID") @PathVariable String iccid) {

        logger.debug("Getting eSIM by ICCID: {}", iccid);

        MockEsim esim = esimService.getEsimByIccid(iccid);
        return ResponseEntity.ok(mapEsimToDetailResponse(esim));
    }

    @GetMapping
    @Operation(summary = "List eSIMs", description = "Get all eSIMs with optional status filter")
    public ResponseEntity<Map<String, Object>> listEsims(
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        logger.debug("Listing eSIMs - status: {}, page: {}, size: {}", status, page, size);

        Page<MockEsim> esimPage;
        if (status != null && !status.isEmpty()) {
            esimPage = esimService.getEsimsByStatus(status, page, size);
        } else {
            esimPage = esimService.getAllEsims(page, size);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("esims", esimPage.getContent().stream()
            .map(this::mapEsimToListResponse)
            .toList());
        response.put("pagination", Map.of(
            "page", esimPage.getNumber(),
            "size", esimPage.getSize(),
            "totalElements", esimPage.getTotalElements(),
            "totalPages", esimPage.getTotalPages()
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{esimId}/bundles")
    @Operation(summary = "Attach bundle", description = "Attach an additional bundle to an existing eSIM (top-off)")
    public ResponseEntity<Map<String, Object>> attachBundle(
            @Parameter(description = "eSIM ID") @PathVariable String esimId,
            @Valid @RequestBody AttachBundleRequest request) {

        logger.info("Attaching bundle {} to eSIM {}", request.getBundleId(), esimId);

        MockEsim esim = esimService.attachBundle(esimId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("esimId", esim.getEsimId());
        response.put("bundleId", request.getBundleId());
        response.put("status", "attached");
        response.put("activationDate", esim.getActivationDate());
        response.put("totalRemainingDataMB", esim.getTotalRemainingDataMB());
        response.put("attachedBundles", esim.getAttachedBundles());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{esimId}")
    @Operation(summary = "Deactivate eSIM", description = "Deactivate an eSIM")
    public ResponseEntity<Map<String, Object>> deactivateEsim(
            @Parameter(description = "eSIM ID") @PathVariable String esimId) {

        logger.info("Deactivating eSIM: {}", esimId);

        MockEsim esim = esimService.deactivateEsim(esimId);

        return ResponseEntity.ok(Map.of(
            "esimId", esim.getEsimId(),
            "status", esim.getStatus(),
            "deactivationDate", esim.getUpdatedAt()
        ));
    }

    private Map<String, Object> mapEsimToDetailResponse(MockEsim esim) {
        Map<String, Object> response = new HashMap<>();
        response.put("esimId", esim.getEsimId());
        response.put("iccid", esim.getIccid());
        response.put("matchingId", esim.getMatchingId());
        response.put("qrCodeUrl", esim.getQrCodeUrl());
        response.put("status", esim.getStatus());
        response.put("activationDate", esim.getActivationDate());
        response.put("totalDataAllowanceMB", esim.getTotalDataAllowanceMB());
        response.put("totalDataUsedMB", esim.getTotalDataUsedMB());
        response.put("totalRemainingDataMB", esim.getTotalRemainingDataMB());
        response.put("lastUsed", esim.getLastUsed());
        response.put("attachedBundles", esim.getAttachedBundles());
        response.put("network", esim.getNetwork());
        response.put("createdAt", esim.getCreatedAt());
        response.put("updatedAt", esim.getUpdatedAt());
        return response;
    }

    private Map<String, Object> mapEsimToListResponse(MockEsim esim) {
        Map<String, Object> response = new HashMap<>();
        response.put("esimId", esim.getEsimId());
        response.put("iccid", esim.getIccid());
        response.put("status", esim.getStatus());
        response.put("totalRemainingDataMB", esim.getTotalRemainingDataMB());
        response.put("bundleCount", esim.getAttachedBundles() != null ? esim.getAttachedBundles().size() : 0);
        response.put("createdAt", esim.getCreatedAt());
        return response;
    }
}
