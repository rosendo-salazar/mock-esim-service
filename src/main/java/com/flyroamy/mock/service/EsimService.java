package com.flyroamy.mock.service;

import com.flyroamy.mock.dto.request.AttachBundleRequest;
import com.flyroamy.mock.dto.request.ProvisionEsimRequest;
import com.flyroamy.mock.exception.EsimExpiredException;
import com.flyroamy.mock.exception.EsimNotFoundException;
import com.flyroamy.mock.exception.InvalidRequestException;
import com.flyroamy.mock.model.MockBundle;
import com.flyroamy.mock.model.MockEsim;
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

@Service
public class EsimService {

    private static final Logger logger = LoggerFactory.getLogger(EsimService.class);

    private final MockEsimRepository esimRepository;
    private final BundleService bundleService;
    private final QrCodeService qrCodeService;
    private final IccidGenerator iccidGenerator;
    private final MatchingIdGenerator matchingIdGenerator;

    public EsimService(
            MockEsimRepository esimRepository,
            BundleService bundleService,
            QrCodeService qrCodeService,
            IccidGenerator iccidGenerator,
            MatchingIdGenerator matchingIdGenerator) {
        this.esimRepository = esimRepository;
        this.bundleService = bundleService;
        this.qrCodeService = qrCodeService;
        this.iccidGenerator = iccidGenerator;
        this.matchingIdGenerator = matchingIdGenerator;
    }

    /**
     * Provision a new eSIM
     */
    public MockEsim provisionEsim(ProvisionEsimRequest request) {
        logger.info("Provisioning new eSIM for bundle: {}", request.getBundleId());

        // Validate bundle exists
        MockBundle bundle = bundleService.getBundleById(request.getBundleId());

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

        // Attach initial bundle
        MockEsim.AttachedBundle attachedBundle = createAttachedBundle(bundle);
        esim.addBundle(attachedBundle);

        // Set activation date and status to active
        esim.setActivationDate(LocalDateTime.now());
        esim.setStatus("active");

        MockEsim saved = esimRepository.save(esim);
        logger.info("Provisioned eSIM: {} with ICCID: {}", saved.getEsimId(), saved.getIccid());

        return saved;
    }

    /**
     * Attach an additional bundle to an existing eSIM (top-off)
     */
    public MockEsim attachBundle(String esimId, AttachBundleRequest request) {
        logger.info("Attaching bundle {} to eSIM {}", request.getBundleId(), esimId);

        MockEsim esim = getEsimById(esimId);

        // Validate eSIM status
        if ("deactivated".equals(esim.getStatus())) {
            throw new InvalidRequestException("Cannot attach bundle to deactivated eSIM", Map.of("esimId", esimId));
        }
        if ("expired".equals(esim.getStatus())) {
            throw new EsimExpiredException(esimId);
        }

        // Validate bundle exists
        MockBundle bundle = bundleService.getBundleById(request.getBundleId());

        // Create and attach bundle
        MockEsim.AttachedBundle attachedBundle = createAttachedBundle(bundle);
        esim.addBundle(attachedBundle);

        // Update last activity
        esim.setUpdatedAt(LocalDateTime.now());
        if (esim.getStatus().equals("provisioned")) {
            esim.setStatus("active");
            esim.setActivationDate(LocalDateTime.now());
        }

        MockEsim saved = esimRepository.save(esim);
        logger.info("Attached bundle {} to eSIM {}. Total bundles: {}",
            request.getBundleId(), esimId, saved.getAttachedBundles().size());

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

        // Mark all attached bundles as deactivated
        for (MockEsim.AttachedBundle bundle : esim.getAttachedBundles()) {
            bundle.setStatus("deactivated");
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

        // Consume from active bundles (FIFO order)
        for (MockEsim.AttachedBundle bundle : esim.getAttachedBundles()) {
            if (!"active".equals(bundle.getStatus()) || remainingUsage <= 0) {
                continue;
            }

            int available = bundle.getRemainingDataMB() != null ? bundle.getRemainingDataMB() : 0;
            int consumed = Math.min(available, remainingUsage);

            bundle.setDataUsedMB((bundle.getDataUsedMB() != null ? bundle.getDataUsedMB() : 0) + consumed);
            bundle.setRemainingDataMB(available - consumed);
            remainingUsage -= consumed;

            // Check if bundle is depleted
            if (bundle.getRemainingDataMB() <= 0) {
                bundle.setStatus("depleted");
                logger.info("Bundle {} depleted on eSIM {}", bundle.getBundleId(), esimId);
            }
        }

        esim.recalculateTotals();
        esim.setLastUsed(LocalDateTime.now());
        esim.setUpdatedAt(LocalDateTime.now());

        // Check if all bundles are depleted
        boolean allDepleted = esim.getAttachedBundles().stream()
            .allMatch(b -> !"active".equals(b.getStatus()));
        if (allDepleted) {
            logger.info("All bundles depleted on eSIM {}", esimId);
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
     * Delete all eSIMs (admin reset)
     */
    public void deleteAll() {
        logger.warn("Deleting all eSIMs");
        esimRepository.deleteAll();
    }

    // Helper method to create AttachedBundle from MockBundle
    private MockEsim.AttachedBundle createAttachedBundle(MockBundle bundle) {
        MockEsim.AttachedBundle attached = new MockEsim.AttachedBundle();
        attached.setBundleId(bundle.getBundleId());
        attached.setBundleName(bundle.getName());
        attached.setAttachedAt(LocalDateTime.now());
        attached.setExpiryDate(LocalDateTime.now().plusDays(bundle.getValidityDays()));

        int dataMB = (int) (bundle.getDataGB() * 1024);
        attached.setDataAllowanceMB(dataMB);
        attached.setDataUsedMB(0);
        attached.setRemainingDataMB(dataMB);
        attached.setStatus("active");
        attached.setCountries(bundle.getCountries());
        attached.setPackageType(bundle.getPackageType());

        return attached;
    }
}
