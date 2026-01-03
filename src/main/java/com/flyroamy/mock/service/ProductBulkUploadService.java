package com.flyroamy.mock.service;

import com.flyroamy.mock.dto.request.BulkProductUploadRequest;
import com.flyroamy.mock.dto.request.BulkProductUploadRequest.ProductUploadItem;
import com.flyroamy.mock.model.MockProduct;
import com.flyroamy.mock.repository.MockProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for bulk uploading products.
 * Handles Excel format data conversion and validation.
 */
@Service
public class ProductBulkUploadService {

    private static final Logger logger = LoggerFactory.getLogger(ProductBulkUploadService.class);

    private final MockProductRepository productRepository;

    public ProductBulkUploadService(MockProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Process bulk upload of products.
     * Handles both create and update operations based on productId.
     */
    public BulkUploadResult processBulkUpload(BulkProductUploadRequest request) {
        BulkUploadResult result = new BulkUploadResult();

        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            result.addError("No products provided");
            return result;
        }

        logger.info("Processing bulk upload of {} products", request.getProducts().size());

        for (int i = 0; i < request.getProducts().size(); i++) {
            ProductUploadItem item = request.getProducts().get(i);
            int rowNumber = i + 1;

            try {
                processProductItem(item, rowNumber, result);
            } catch (Exception e) {
                logger.error("Error processing row {}: {}", rowNumber, e.getMessage());
                result.addError("Row " + rowNumber + ": " + e.getMessage());
            }
        }

        logger.info("Bulk upload completed - created: {}, updated: {}, errors: {}",
            result.getCreated(), result.getUpdated(), result.getErrors().size());

        return result;
    }

    /**
     * Process a single product item from the upload
     */
    private void processProductItem(ProductUploadItem item, int rowNumber, BulkUploadResult result) {
        // Normalize the item (handle Excel format fields)
        normalizeItem(item);

        // Validate required fields
        List<String> validationErrors = validateItem(item);
        if (!validationErrors.isEmpty()) {
            for (String error : validationErrors) {
                result.addError("Row " + rowNumber + ": " + error);
            }
            return;
        }

        // Check if product exists (by productId)
        String productId = item.getProductId();
        Optional<MockProduct> existingOpt = productRepository.findByProductId(productId);

        if (existingOpt.isPresent()) {
            // Update existing product
            MockProduct existing = existingOpt.get();
            updateProductFromItem(existing, item);
            productRepository.save(existing);
            result.incrementUpdated();
            logger.debug("Updated product: {} (row {})", productId, rowNumber);
        } else {
            // Create new product
            MockProduct newProduct = createProductFromItem(item);
            productRepository.save(newProduct);
            result.incrementCreated();
            logger.debug("Created product: {} (row {})", productId, rowNumber);
        }
    }

    /**
     * Normalize item fields - handle Excel format to API format conversion
     */
    private void normalizeItem(ProductUploadItem item) {
        // Convert data (GB) to dataQuotaMb
        if (item.getDataQuotaMb() == null && item.getData() != null) {
            item.setDataQuotaMb((int) (item.getData() * 1024));
        }

        // Convert days to validityDays
        if (item.getValidityDays() == null && item.getDays() != null) {
            item.setValidityDays(item.getDays());
        }

        // Convert wholesaleCost to wholesalePriceUsd
        if (item.getWholesalePriceUsd() == null && item.getWholesaleCost() != null) {
            item.setWholesalePriceUsd(item.getWholesaleCost());
        }

        // Convert price to rrpUsd
        if (item.getRrpUsd() == null && item.getPrice() != null) {
            item.setRrpUsd(item.getPrice());
        }

        // Convert countries string to countriesEnabled list
        if ((item.getCountriesEnabled() == null || item.getCountriesEnabled().isEmpty())
                && item.getCountries() != null && !item.getCountries().isEmpty()) {
            List<String> countryList = Arrays.stream(item.getCountries().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toList());
            item.setCountriesEnabled(countryList);
        }

        // Calculate dataQuotaBytes from dataQuotaMb
        if (item.getDataQuotaBytes() == null && item.getDataQuotaMb() != null) {
            item.setDataQuotaBytes((long) item.getDataQuotaMb() * 1024 * 1024);
        }

        // Classify package type based on countries if not provided
        if (item.getPackageType() == null || item.getPackageType().isEmpty()) {
            item.setPackageType(classifyPackageType(item.getCountriesEnabled()));
        }

        // Default isActive to true
        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }
    }

    /**
     * Classify package type based on country count
     */
    private String classifyPackageType(List<String> countries) {
        if (countries == null || countries.isEmpty()) {
            return "country";
        }
        int count = countries.size();
        if (count == 1) {
            return "country";
        }
        if (count >= 100) {
            return "global";
        }
        return "regional";
    }

    /**
     * Validate item has required fields
     */
    private List<String> validateItem(ProductUploadItem item) {
        List<String> errors = new ArrayList<>();

        if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
            errors.add("productId is required");
        }

        if (item.getName() == null || item.getName().trim().isEmpty()) {
            errors.add("name is required");
        }

        if (item.getDataQuotaMb() == null || item.getDataQuotaMb() <= 0) {
            errors.add("dataQuotaMb (or data in GB) is required and must be positive");
        }

        if (item.getValidityDays() == null || item.getValidityDays() <= 0) {
            errors.add("validityDays (or days) is required and must be positive");
        }

        return errors;
    }

    /**
     * Create a new MockProduct from upload item
     */
    private MockProduct createProductFromItem(ProductUploadItem item) {
        MockProduct product = new MockProduct();

        product.setProductId(item.getProductId());
        product.setUid("prod_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        product.setName(item.getName());
        product.setDescription(item.getDescription());
        product.setCountriesEnabled(item.getCountriesEnabled());
        product.setCountries(item.getCountriesEnabled());
        product.setDataQuotaMb(item.getDataQuotaMb());
        product.setDataQuotaBytes(item.getDataQuotaBytes());
        product.setValidityDays(item.getValidityDays());
        product.setPolicyId(item.getPolicyId());
        product.setPolicyName(item.getPolicyName());
        product.setWholesalePriceUsd(item.getWholesalePriceUsd());
        product.setRrpUsd(item.getRrpUsd());
        product.setRrpEur(item.getRrpEur());
        product.setRrpGbp(item.getRrpGbp());
        product.setRrpCad(item.getRrpCad());
        product.setRrpAud(item.getRrpAud());
        product.setRrpJpy(item.getRrpJpy());
        product.setPackageType(item.getPackageType());
        product.setRegion(item.getRegion());
        product.setActive(item.getIsActive() != null ? item.getIsActive() : true);
        product.setUnlimitedType(item.getUnlimitedType());
        product.setTerms(item.getTerms());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return product;
    }

    /**
     * Update existing product from upload item
     */
    private void updateProductFromItem(MockProduct product, ProductUploadItem item) {
        product.setName(item.getName());
        product.setDescription(item.getDescription());
        product.setCountriesEnabled(item.getCountriesEnabled());
        product.setCountries(item.getCountriesEnabled());
        product.setDataQuotaMb(item.getDataQuotaMb());
        product.setDataQuotaBytes(item.getDataQuotaBytes());
        product.setValidityDays(item.getValidityDays());

        if (item.getPolicyId() != null) product.setPolicyId(item.getPolicyId());
        if (item.getPolicyName() != null) product.setPolicyName(item.getPolicyName());
        if (item.getWholesalePriceUsd() != null) product.setWholesalePriceUsd(item.getWholesalePriceUsd());
        if (item.getRrpUsd() != null) product.setRrpUsd(item.getRrpUsd());
        if (item.getRrpEur() != null) product.setRrpEur(item.getRrpEur());
        if (item.getRrpGbp() != null) product.setRrpGbp(item.getRrpGbp());
        if (item.getRrpCad() != null) product.setRrpCad(item.getRrpCad());
        if (item.getRrpAud() != null) product.setRrpAud(item.getRrpAud());
        if (item.getRrpJpy() != null) product.setRrpJpy(item.getRrpJpy());
        if (item.getPackageType() != null) product.setPackageType(item.getPackageType());
        if (item.getRegion() != null) product.setRegion(item.getRegion());
        if (item.getIsActive() != null) product.setActive(item.getIsActive());
        if (item.getUnlimitedType() != null) product.setUnlimitedType(item.getUnlimitedType());
        if (item.getTerms() != null) product.setTerms(item.getTerms());

        product.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Result of bulk upload operation
     */
    public static class BulkUploadResult {
        private int created = 0;
        private int updated = 0;
        private List<String> errors = new ArrayList<>();

        public void incrementCreated() { created++; }
        public void incrementUpdated() { updated++; }
        public void addError(String error) { errors.add(error); }

        public int getCreated() { return created; }
        public int getUpdated() { return updated; }
        public List<String> getErrors() { return errors; }
        public int getTotal() { return created + updated; }
        public boolean isSuccess() { return errors.isEmpty(); }
    }
}
