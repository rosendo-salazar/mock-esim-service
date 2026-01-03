package com.flyroamy.mock.service;

import com.flyroamy.mock.exception.ProductNotFoundException;
import com.flyroamy.mock.model.MockProduct;
import com.flyroamy.mock.repository.MockProductRepository;
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
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final MockProductRepository productRepository;

    public ProductService(MockProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Get all active products with pagination
     */
    public Page<MockProduct> getAllProducts(int page, int size) {
        logger.debug("Fetching all products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findByIsActiveTrue(pageable);
    }

    /**
     * Get all active products (no pagination)
     */
    public List<MockProduct> getAllProducts() {
        logger.debug("Fetching all active products");
        return productRepository.findByIsActiveTrue();
    }

    /**
     * Get product by ID
     */
    public MockProduct getProductById(String productId) {
        logger.debug("Fetching product by ID: {}", productId);
        return productRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    /**
     * Get product by UID
     */
    public MockProduct getProductByUid(String uid) {
        logger.debug("Fetching product by UID: {}", uid);
        return productRepository.findByUid(uid)
            .orElseThrow(() -> new ProductNotFoundException(uid));
    }

    /**
     * Get product by ID (optional)
     */
    public Optional<MockProduct> findProductById(String productId) {
        return productRepository.findByProductId(productId);
    }

    /**
     * Get product by ID or UID (unified lookup)
     * Tries productId first, then falls back to uid
     */
    public MockProduct getProductByIdOrUid(String identifier) {
        logger.debug("Looking up product by identifier: {}", identifier);

        // Try productId first
        Optional<MockProduct> product = productRepository.findByProductId(identifier);
        if (product.isPresent()) {
            logger.debug("Found product by productId: {}", identifier);
            return product.get();
        }

        // Fall back to uid
        logger.debug("ProductId not found, trying uid lookup for: {}", identifier);
        return productRepository.findByUid(identifier)
            .orElseThrow(() -> new ProductNotFoundException(identifier));
    }

    /**
     * Get products by country code
     */
    public Page<MockProduct> getProductsByCountry(String countryCode, int page, int size) {
        logger.debug("Fetching products for country: {}", countryCode);
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());

        // Try both countries and countries_enabled fields
        Page<MockProduct> products = productRepository.findByCountryAndActive(countryCode.toUpperCase(), pageable);
        if (products.isEmpty()) {
            products = productRepository.findByCountriesEnabledAndActive(countryCode.toUpperCase(), pageable);
        }

        return products;
    }

    /**
     * Get products by region
     */
    public Page<MockProduct> getProductsByRegion(String region, int page, int size) {
        logger.debug("Fetching products for region: {}", region);
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return productRepository.findByRegionAndActive(region.toLowerCase(), pageable);
    }

    /**
     * Get products by package type
     */
    public Page<MockProduct> getProductsByPackageType(String packageType, int page, int size) {
        logger.debug("Fetching products by package type: {}", packageType);
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return productRepository.findByPackageTypeAndActive(packageType.toLowerCase(), pageable);
    }

    /**
     * Create a new product
     */
    public MockProduct createProduct(MockProduct product) {
        logger.info("Creating new product: {}", product.getName());

        // Generate UID if not provided
        if (product.getUid() == null || product.getUid().isEmpty()) {
            product.setUid("prod_" + UUID.randomUUID().toString().substring(0, 12));
        }

        // Generate productId if not provided
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            product.setProductId(product.getUid());
        }

        if (productRepository.existsByProductId(product.getProductId())) {
            throw new IllegalArgumentException("Product with ID " + product.getProductId() + " already exists");
        }

        // Calculate data_quota_bytes from data_quota_mb if not provided
        if (product.getDataQuotaBytes() == null && product.getDataQuotaMb() != null) {
            product.setDataQuotaBytes((long) product.getDataQuotaMb() * 1024 * 1024);
        }

        // Calculate data_quota_mb from dataGB for backward compatibility
        if (product.getDataQuotaMb() == null && product.getDataGB() != null) {
            product.setDataQuotaMb((int) (product.getDataGB() * 1024));
            product.setDataQuotaBytes((long) (product.getDataGB() * 1024 * 1024 * 1024));
        }

        // Sync countries and countries_enabled
        if (product.getCountriesEnabled() == null && product.getCountries() != null) {
            product.setCountriesEnabled(product.getCountries());
        }
        if (product.getCountries() == null && product.getCountriesEnabled() != null) {
            product.setCountries(product.getCountriesEnabled());
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        MockProduct saved = productRepository.save(product);
        logger.info("Created product: {} with ID: {}", saved.getName(), saved.getProductId());
        return saved;
    }

    /**
     * Update an existing product
     */
    public MockProduct updateProduct(String productId, MockProduct updates) {
        logger.info("Updating product: {}", productId);

        MockProduct existing = getProductById(productId);

        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getDataQuotaMb() != null) {
            existing.setDataQuotaMb(updates.getDataQuotaMb());
            existing.setDataQuotaBytes((long) updates.getDataQuotaMb() * 1024 * 1024);
        }
        if (updates.getDataGB() != null) existing.setDataGB(updates.getDataGB());
        if (updates.getValidityDays() != null) existing.setValidityDays(updates.getValidityDays());
        if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
        if (updates.getPrices() != null) existing.setPrices(updates.getPrices());
        if (updates.getWholesaleCost() != null) existing.setWholesaleCost(updates.getWholesaleCost());
        if (updates.getWholesalePriceUsd() != null) existing.setWholesalePriceUsd(updates.getWholesalePriceUsd());
        if (updates.getRrpUsd() != null) existing.setRrpUsd(updates.getRrpUsd());
        if (updates.getRrpEur() != null) existing.setRrpEur(updates.getRrpEur());
        if (updates.getRrpGbp() != null) existing.setRrpGbp(updates.getRrpGbp());
        if (updates.getRrpCad() != null) existing.setRrpCad(updates.getRrpCad());
        if (updates.getRrpAud() != null) existing.setRrpAud(updates.getRrpAud());
        if (updates.getRrpJpy() != null) existing.setRrpJpy(updates.getRrpJpy());
        if (updates.getPackageType() != null) existing.setPackageType(updates.getPackageType());
        if (updates.getCountries() != null) {
            existing.setCountries(updates.getCountries());
            existing.setCountriesEnabled(updates.getCountries());
        }
        if (updates.getCountriesEnabled() != null) {
            existing.setCountriesEnabled(updates.getCountriesEnabled());
            existing.setCountries(updates.getCountriesEnabled());
        }
        if (updates.getRegion() != null) existing.setRegion(updates.getRegion());
        if (updates.getPolicyId() != null) existing.setPolicyId(updates.getPolicyId());
        if (updates.getPolicyName() != null) existing.setPolicyName(updates.getPolicyName());

        existing.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(existing);
    }

    /**
     * Delete a product
     */
    public void deleteProduct(String productId) {
        logger.info("Deleting product: {}", productId);

        if (!productRepository.existsByProductId(productId)) {
            throw new ProductNotFoundException(productId);
        }

        productRepository.deleteByProductId(productId);
        logger.info("Deleted product: {}", productId);
    }

    /**
     * Toggle product active status
     */
    public MockProduct toggleProductStatus(String productId, boolean isActive) {
        logger.info("Toggling product {} status to: {}", productId, isActive);

        MockProduct product = getProductById(productId);
        product.setActive(isActive);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     * Get product count
     */
    public long getProductCount() {
        return productRepository.count();
    }

    /**
     * Check if product exists
     */
    public boolean productExists(String productId) {
        return productRepository.existsByProductId(productId);
    }

    /**
     * Delete all products (admin reset)
     */
    public void deleteAll() {
        logger.warn("Deleting all products");
        productRepository.deleteAll();
    }

}
