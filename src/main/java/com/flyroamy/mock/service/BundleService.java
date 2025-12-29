package com.flyroamy.mock.service;

import com.flyroamy.mock.exception.BundleNotFoundException;
import com.flyroamy.mock.model.MockBundle;
import com.flyroamy.mock.repository.MockBundleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BundleService {

    private static final Logger logger = LoggerFactory.getLogger(BundleService.class);

    private final MockBundleRepository bundleRepository;

    public BundleService(MockBundleRepository bundleRepository) {
        this.bundleRepository = bundleRepository;
    }

    /**
     * Get all active bundles with pagination
     */
    public Page<MockBundle> getAllBundles(int page, int size) {
        logger.debug("Fetching all bundles - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return bundleRepository.findByIsActiveTrue(pageable);
    }

    /**
     * Get all active bundles (no pagination)
     */
    public List<MockBundle> getAllBundles() {
        logger.debug("Fetching all active bundles");
        return bundleRepository.findByIsActiveTrue();
    }

    /**
     * Get bundle by ID
     */
    public MockBundle getBundleById(String bundleId) {
        logger.debug("Fetching bundle by ID: {}", bundleId);
        return bundleRepository.findByBundleId(bundleId)
            .orElseThrow(() -> new BundleNotFoundException(bundleId));
    }

    /**
     * Get bundle by ID (optional)
     */
    public Optional<MockBundle> findBundleById(String bundleId) {
        return bundleRepository.findByBundleId(bundleId);
    }

    /**
     * Get bundles by country code
     */
    public Page<MockBundle> getBundlesByCountry(String countryCode, int page, int size) {
        logger.debug("Fetching bundles for country: {}", countryCode);
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return bundleRepository.findByCountryAndActive(countryCode.toLowerCase(), pageable);
    }

    /**
     * Get bundles by region
     */
    public Page<MockBundle> getBundlesByRegion(String region, int page, int size) {
        logger.debug("Fetching bundles for region: {}", region);
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return bundleRepository.findByRegionAndActive(region.toLowerCase(), pageable);
    }

    /**
     * Get bundles by package type
     */
    public Page<MockBundle> getBundlesByPackageType(String packageType, int page, int size) {
        logger.debug("Fetching bundles by package type: {}", packageType);
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return bundleRepository.findByPackageTypeAndActive(packageType.toLowerCase(), pageable);
    }

    /**
     * Create a new bundle
     */
    public MockBundle createBundle(MockBundle bundle) {
        logger.info("Creating new bundle: {}", bundle.getName());

        if (bundleRepository.existsByBundleId(bundle.getBundleId())) {
            throw new IllegalArgumentException("Bundle with ID " + bundle.getBundleId() + " already exists");
        }

        bundle.setCreatedAt(LocalDateTime.now());
        bundle.setUpdatedAt(LocalDateTime.now());

        MockBundle saved = bundleRepository.save(bundle);
        logger.info("Created bundle: {} with ID: {}", saved.getName(), saved.getBundleId());
        return saved;
    }

    /**
     * Update an existing bundle
     */
    public MockBundle updateBundle(String bundleId, MockBundle updates) {
        logger.info("Updating bundle: {}", bundleId);

        MockBundle existing = getBundleById(bundleId);

        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getDataGB() != null) existing.setDataGB(updates.getDataGB());
        if (updates.getValidityDays() != null) existing.setValidityDays(updates.getValidityDays());
        if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
        if (updates.getPrices() != null) existing.setPrices(updates.getPrices());
        if (updates.getWholesaleCost() != null) existing.setWholesaleCost(updates.getWholesaleCost());
        if (updates.getPackageType() != null) existing.setPackageType(updates.getPackageType());
        if (updates.getCountries() != null) existing.setCountries(updates.getCountries());
        if (updates.getRegion() != null) existing.setRegion(updates.getRegion());
        if (updates.getBadge() != null) existing.setBadge(updates.getBadge());

        existing.setUpdatedAt(LocalDateTime.now());

        return bundleRepository.save(existing);
    }

    /**
     * Delete a bundle
     */
    public void deleteBundle(String bundleId) {
        logger.info("Deleting bundle: {}", bundleId);

        if (!bundleRepository.existsByBundleId(bundleId)) {
            throw new BundleNotFoundException(bundleId);
        }

        bundleRepository.deleteByBundleId(bundleId);
        logger.info("Deleted bundle: {}", bundleId);
    }

    /**
     * Toggle bundle active status
     */
    public MockBundle toggleBundleStatus(String bundleId, boolean isActive) {
        logger.info("Toggling bundle {} status to: {}", bundleId, isActive);

        MockBundle bundle = getBundleById(bundleId);
        bundle.setActive(isActive);
        bundle.setUpdatedAt(LocalDateTime.now());

        return bundleRepository.save(bundle);
    }

    /**
     * Get bundle count
     */
    public long getBundleCount() {
        return bundleRepository.count();
    }

    /**
     * Check if bundle exists
     */
    public boolean bundleExists(String bundleId) {
        return bundleRepository.existsByBundleId(bundleId);
    }
}
