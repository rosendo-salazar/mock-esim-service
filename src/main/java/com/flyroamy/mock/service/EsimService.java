package com.flyroamy.mock.service;

import com.flyroamy.mock.dto.request.CreateEsimRequest;
import com.flyroamy.mock.dto.request.ProvisionEsimRequest;
import com.flyroamy.mock.exception.EsimExpiredException;
import com.flyroamy.mock.exception.EsimNotFoundException;
import com.flyroamy.mock.exception.InvalidRequestException;
import com.flyroamy.mock.model.MockEsim;
import com.flyroamy.mock.model.MockProduct;
import com.flyroamy.mock.repository.MockEsimRepository;
import com.flyroamy.mock.util.IccidGenerator;
import com.flyroamy.mock.util.MatchingIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EsimService {

    private static final Logger logger = LoggerFactory.getLogger(EsimService.class);

    private final MockEsimRepository esimRepository;
    private final ProductService productService;
    private final QrCodeService qrCodeService;
    private final IccidGenerator iccidGenerator;
    private final MatchingIdGenerator matchingIdGenerator;

    public EsimService(
            MockEsimRepository esimRepository,
            ProductService productService,
            QrCodeService qrCodeService,
            IccidGenerator iccidGenerator,
            MatchingIdGenerator matchingIdGenerator) {
        this.esimRepository = esimRepository;
        this.productService = productService;
        this.qrCodeService = qrCodeService;
        this.iccidGenerator = iccidGenerator;
        this.matchingIdGenerator = matchingIdGenerator;
    }

    /**
     * Provision a new eSIM
     */
    public MockEsim provisionEsim(ProvisionEsimRequest request) {
        logger.info("Provisioning new eSIM for product: {}", request.getProductId());

        // Validate product exists
        MockProduct product = productService.getProductById(request.getProductId());

        // Check for idempotency - if orderId is provided, check if eSIM already exists
        if (request.getMetadata() != null && request.getMetadata().containsKey("orderId")) {
            String orderId = (String) request.getMetadata().get("orderId");
            Optional<MockEsim> existing = esimRepository.findByOrderId(orderId);
            if (existing.isPresent()) {
                logger.info("eSIM already exists for order {}, returning existing", orderId);
                return existing.get();
            }
        }

        // Generate eSIM identifiers
        String esimId = matchingIdGenerator.generateEsimId();
        String iccid = iccidGenerator.generate();
        String matchingId = matchingIdGenerator.generate();
        String activationCode = matchingIdGenerator.generateActivationCode();
        String qrCodeData = matchingIdGenerator.generateLpaString(activationCode);
        String qrCodeUrl = qrCodeService.generateQrCodeUrl(esimId);

        // Create eSIM
        MockEsim esim = new MockEsim();
        esim.setEsimId(esimId);
        esim.setIccid(iccid);
        esim.setMatchingId(matchingId);
        esim.setActivationCode(activationCode);
        esim.setQrCodeData(qrCodeData);
        esim.setQrCodeUrl(qrCodeUrl);
        esim.setStatus("provisioned");
        esim.setUserEmail(request.getUserEmail());
        esim.setProfileType(request.getProfileType() != null ? request.getProfileType() : "consumer");
        esim.setMetadata(request.getMetadata());

        // Attach initial plan
        MockEsim.AttachedPlan attachedPlan = createAttachedPlan(product);
        esim.addPlan(attachedPlan);

        // Set activation date and status to active
        esim.setActivationDate(LocalDateTime.now());
        esim.setStatus("active");

        MockEsim saved = esimRepository.save(esim);
        logger.info("Provisioned eSIM: {} with ICCID: {}", saved.getEsimId(), saved.getIccid());

        return saved;
    }

    /**
     * Get eSIM by ID
     */
    public MockEsim getEsimById(String esimId) {
        return esimRepository.findByEsimId(esimId)
            .orElseThrow(() -> new EsimNotFoundException(esimId));
    }

    /**
     * Get eSIM by ID (optional)
     */
    public Optional<MockEsim> findEsimById(String esimId) {
        return esimRepository.findByEsimId(esimId);
    }

    /**
     * Get eSIM by ICCID
     */
    public MockEsim getEsimByIccid(String iccid) {
        return esimRepository.findByIccid(iccid)
            .orElseThrow(() -> new EsimNotFoundException("ICCID: " + iccid));
    }

    /**
     * Get all eSIMs with pagination
     */
    public Page<MockEsim> getAllEsims(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return esimRepository.findAll(pageable);
    }

    /**
     * Get eSIMs by status
     */
    public Page<MockEsim> getEsimsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return esimRepository.findByStatus(status, pageable);
    }

    /**
     * Deactivate an eSIM
     */
    public MockEsim deactivateEsim(String esimId) {
        logger.info("Deactivating eSIM: {}", esimId);

        MockEsim esim = getEsimById(esimId);
        esim.setStatus("deactivated");
        esim.setUpdatedAt(LocalDateTime.now());

        // Mark all attached plans as deactivated
        for (MockEsim.AttachedPlan plan : esim.getAttachedPlans()) {
            plan.setStatus("deactivated");
        }

        MockEsim saved = esimRepository.save(esim);
        logger.info("Deactivated eSIM: {}", esimId);

        return saved;
    }

    /**
     * Simulate data usage on an eSIM
     */
    public MockEsim simulateUsage(String esimId, int usageMB) {
        logger.info("Simulating {} MB usage on eSIM: {}", usageMB, esimId);

        MockEsim esim = getEsimById(esimId);

        if (!"active".equals(esim.getStatus())) {
            throw new InvalidRequestException("Cannot simulate usage on non-active eSIM",
                Map.of("esimId", esimId, "status", esim.getStatus()));
        }

        int remainingUsage = usageMB;

        // Consume from active plans (FIFO order)
        for (MockEsim.AttachedPlan plan : esim.getAttachedPlans()) {
            if (!"active".equals(plan.getStatus()) || remainingUsage <= 0) {
                continue;
            }

            int available = plan.getRemainingDataMB() != null ? plan.getRemainingDataMB() : 0;
            int consumed = Math.min(available, remainingUsage);

            plan.setDataUsedMB((plan.getDataUsedMB() != null ? plan.getDataUsedMB() : 0) + consumed);
            plan.setRemainingDataMB(available - consumed);
            remainingUsage -= consumed;

            // Check if plan is depleted
            if (plan.getRemainingDataMB() <= 0) {
                plan.setStatus("depleted");
                logger.info("Plan {} depleted on eSIM {}", plan.getProductId(), esimId);
            }
        }

        esim.recalculateTotals();
        esim.setLastUsed(LocalDateTime.now());
        esim.setUpdatedAt(LocalDateTime.now());

        // Check if all plans are depleted
        boolean allDepleted = esim.getAttachedPlans().stream()
            .allMatch(p -> !"active".equals(p.getStatus()));
        if (allDepleted) {
            logger.info("All plans depleted on eSIM {}", esimId);
        }

        return esimRepository.save(esim);
    }

    /**
     * Force status change (admin)
     */
    public MockEsim forceStatusChange(String esimId, String newStatus) {
        logger.info("Forcing status change on eSIM {} to: {}", esimId, newStatus);

        MockEsim esim = getEsimById(esimId);
        esim.setStatus(newStatus);
        esim.setUpdatedAt(LocalDateTime.now());

        return esimRepository.save(esim);
    }

    /**
     * Get eSIM statistics
     */
    public Map<String, Long> getStatistics() {
        return Map.of(
            "total", esimRepository.count(),
            "active", esimRepository.countByStatus("active"),
            "provisioned", esimRepository.countByStatus("provisioned"),
            "deactivated", esimRepository.countByStatus("deactivated"),
            "expired", esimRepository.countByStatus("expired")
        );
    }

    /**
     * Create a new eSIM (Maya API style)
     */
    public MockEsim createEsim(CreateEsimRequest request) {
        logger.info("Creating eSIM - planTypeId: {}, customerId: {}", request.getPlanTypeId(), request.getCustomerId());

        // Generate eSIM identifiers
        String uid = "esim_" + UUID.randomUUID().toString().substring(0, 12);
        String esimId = matchingIdGenerator.generateEsimId();
        String iccid = iccidGenerator.generate();
        String matchingId = matchingIdGenerator.generate();
        String activationCode = matchingIdGenerator.generateActivationCode();
        String manualCode = matchingIdGenerator.generateManualCode();
        String smdpAddress = "smdp.maya.net";
        String qrCodeData = matchingIdGenerator.generateLpaString(activationCode);
        String qrCodeUrl = qrCodeService.generateQrCodeUrl(esimId);

        // Create eSIM
        MockEsim esim = new MockEsim();
        esim.setUid(uid);
        esim.setEsimId(esimId);
        esim.setIccid(iccid);
        esim.setMatchingId(matchingId);
        esim.setActivationCode(activationCode);
        esim.setManualCode(manualCode);
        esim.setSmdpAddress(smdpAddress);
        esim.setAutoApn(true);
        esim.setApn("maya.apn");
        esim.setQrCodeData(qrCodeData);
        esim.setQrCodeUrl(qrCodeUrl);
        esim.setStatus("provisioned");
        esim.setState("provisioned");
        esim.setServiceStatus("active");
        esim.setNetworkStatus("disconnected");
        esim.setCustomerId(request.getCustomerId());
        esim.setTag(request.getTag());
        esim.setDateAssigned(LocalDateTime.now());

        // If plan_type_id is provided, attach the plan
        if (request.getPlanTypeId() != null && !request.getPlanTypeId().isEmpty()) {
            MockProduct product = productService.getProductById(request.getPlanTypeId());
            MockEsim.AttachedPlan plan = createAttachedPlan(product);
            esim.addPlan(plan);
            esim.setStatus("active");
            esim.setState("active");
            esim.setActivationDate(LocalDateTime.now());
        }

        MockEsim saved = esimRepository.save(esim);
        logger.info("Created eSIM: {} with ICCID: {}", saved.getUid(), saved.getIccid());

        return saved;
    }

    /**
     * Attach a plan to an existing eSIM (Maya API style)
     */
    public MockEsim attachPlan(String iccid, String planTypeId) {
        logger.info("Attaching plan {} to eSIM {}", planTypeId, iccid);

        MockEsim esim = getEsimByIccid(iccid);

        // Validate eSIM status
        if ("deactivated".equals(esim.getStatus())) {
            throw new InvalidRequestException("Cannot attach plan to deactivated eSIM", Map.of("iccid", iccid));
        }

        // Get the product
        MockProduct product = productService.getProductById(planTypeId);

        // Create and attach plan
        MockEsim.AttachedPlan plan = createAttachedPlan(product);
        esim.addPlan(plan);

        // Update status if provisioned
        if ("provisioned".equals(esim.getStatus())) {
            esim.setStatus("active");
            esim.setState("active");
            esim.setActivationDate(LocalDateTime.now());
        }

        esim.setUpdatedAt(LocalDateTime.now());

        MockEsim saved = esimRepository.save(esim);
        logger.info("Attached plan {} to eSIM {}. Total plans: {}",
            planTypeId, iccid, saved.getAttachedPlans().size());

        return saved;
    }

    /**
     * Update an existing eSIM
     */
    public MockEsim updateEsim(String iccid, CreateEsimRequest updates) {
        logger.info("Updating eSIM: {}", iccid);

        MockEsim esim = getEsimByIccid(iccid);

        if (updates.getCustomerId() != null) {
            esim.setCustomerId(updates.getCustomerId());
        }
        if (updates.getTag() != null) {
            esim.setTag(updates.getTag());
        }

        esim.setUpdatedAt(LocalDateTime.now());

        return esimRepository.save(esim);
    }

    /**
     * Delete an eSIM by ICCID
     */
    public void deleteEsim(String iccid) {
        logger.info("Deleting eSIM with ICCID: {}", iccid);

        MockEsim esim = getEsimByIccid(iccid);
        esimRepository.delete(esim);

        logger.info("Deleted eSIM with ICCID: {}", iccid);
    }

    /**
     * Delete all eSIMs (admin reset)
     */
    public void deleteAll() {
        logger.warn("Deleting all eSIMs");
        esimRepository.deleteAll();
    }

    // Helper method to create AttachedPlan from MockProduct
    private MockEsim.AttachedPlan createAttachedPlan(MockProduct product) {
        MockEsim.AttachedPlan attached = new MockEsim.AttachedPlan();
        attached.setProductId(product.getProductId());
        attached.setPlanName(product.getName());
        attached.setAttachedAt(LocalDateTime.now());
        attached.setExpiryDate(LocalDateTime.now().plusDays(product.getValidityDays()));

        // Use data_quota_mb if available, otherwise convert from dataGB
        int dataMB;
        if (product.getDataQuotaMb() != null) {
            dataMB = product.getDataQuotaMb();
        } else if (product.getDataGB() != null) {
            dataMB = (int) (product.getDataGB() * 1024);
        } else {
            dataMB = 1024; // Default 1GB
        }

        attached.setDataAllowanceMB(dataMB);
        attached.setDataUsedMB(0);
        attached.setRemainingDataMB(dataMB);
        attached.setStatus("active");

        // Use countries_enabled if available, otherwise use countries
        List<String> countries = product.getCountriesEnabled() != null ?
            product.getCountriesEnabled() : product.getCountries();
        attached.setCountries(countries);

        attached.setPackageType(product.getPackageType());

        return attached;
    }
}
