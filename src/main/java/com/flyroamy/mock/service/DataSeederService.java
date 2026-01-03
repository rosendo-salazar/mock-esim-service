package com.flyroamy.mock.service;

import com.flyroamy.mock.model.MockProduct;
import com.flyroamy.mock.repository.MockProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DataSeederService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeederService.class);

    // 111 countries for global plans
    private static final List<String> GLOBAL_COUNTRIES = List.of(
        "US", "CA", "MX", "GB", "DE", "FR", "IT", "ES", "JP", "KR", "AU",
        "BR", "AR", "CL", "CO", "PE", "NL", "BE", "AT", "PT", "IE", "CH",
        "SE", "NO", "DK", "FI", "PL", "CZ", "HU", "RO", "GR", "TR", "RU",
        "IN", "CN", "HK", "TW", "SG", "MY", "TH", "ID", "PH", "VN", "NZ",
        "ZA", "EG", "NG", "KE", "MA", "AE", "SA", "IL", "QA", "KW", "BH",
        "OM", "JO", "LB", "PK", "BD", "LK", "NP", "MM", "KH", "LA", "BN",
        "MN", "KZ", "UZ", "AZ", "GE", "AM", "UA", "BY", "LT", "LV", "EE",
        "SK", "SI", "HR", "BA", "RS", "BG", "MK", "AL", "ME", "XK", "MD",
        "CY", "MT", "LU", "IS", "LI", "MC", "SM", "AD", "VA", "GT", "HN",
        "SV", "NI", "CR", "PA", "DO", "PR", "CU", "JM", "TT", "BB", "BS"
    );

    private final MockProductRepository productRepository;

    public DataSeederService(MockProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            logger.info("No products found, seeding initial data...");
            seedProducts();
        } else {
            logger.info("Products already exist, skipping seed. Count: {}", productRepository.count());
        }
    }

    public int seedProducts() {
        List<MockProduct> products = createSeedProducts();
        int created = 0;

        for (MockProduct product : products) {
            if (!productRepository.existsByProductId(product.getProductId())) {
                productRepository.save(product);
                created++;
                logger.info("Created product: {}", product.getName());
            }
        }

        logger.info("Seeded {} products", created);
        return created;
    }

    private List<MockProduct> createSeedProducts() {
        return List.of(
            // USA Plans
            createProduct("usa_1gb_7d", 1001, "USA 1GB 7 Days", 1024, 7, 5.99, 2.50, "country", List.of("US"), null),
            createProduct("usa_3gb_15d", 1002, "USA 3GB 15 Days", 3072, 15, 9.99, 4.50, "country", List.of("US"), null),
            createProduct("usa_5gb_30d", 1003, "USA 5GB 30 Days", 5120, 30, 14.99, 7.00, "country", List.of("US"), null),
            createProduct("usa_10gb_30d", 1004, "USA 10GB 30 Days", 10240, 30, 24.99, 12.00, "country", List.of("US"), null),
            createProduct("usa_20gb_30d", 1005, "USA 20GB 30 Days", 20480, 30, 39.99, 20.00, "country", List.of("US"), null),

            // Mexico Plans
            createProduct("mexico_1gb_7d", 2001, "Mexico 1GB 7 Days", 1024, 7, 4.99, 2.00, "country", List.of("MX"), null),
            createProduct("mexico_5gb_30d", 2002, "Mexico 5GB 30 Days", 5120, 30, 12.99, 6.00, "country", List.of("MX"), null),
            createProduct("mexico_10gb_30d", 2003, "Mexico 10GB 30 Days", 10240, 30, 19.99, 10.00, "country", List.of("MX"), null),

            // Canada Plans
            createProduct("canada_3gb_15d", 3001, "Canada 3GB 15 Days", 3072, 15, 11.99, 5.50, "country", List.of("CA"), null),
            createProduct("canada_5gb_30d", 3002, "Canada 5GB 30 Days", 5120, 30, 16.99, 8.00, "country", List.of("CA"), null),

            // Europe Region
            createProduct("europe_3gb_15d", 4001, "Europe 3GB 15 Days", 3072, 15, 14.99, 7.00, "region",
                List.of("DE", "FR", "IT", "ES", "NL", "BE", "AT", "PT", "IE", "GB"), "europe"),
            createProduct("europe_5gb_30d", 4002, "Europe 5GB 30 Days", 5120, 30, 24.99, 12.00, "region",
                List.of("DE", "FR", "IT", "ES", "NL", "BE", "AT", "PT", "IE", "GB"), "europe"),
            createProduct("europe_10gb_30d", 4003, "Europe 10GB 30 Days", 10240, 30, 39.99, 20.00, "region",
                List.of("DE", "FR", "IT", "ES", "NL", "BE", "AT", "PT", "IE", "GB"), "europe"),

            // Asia Region
            createProduct("asia_3gb_15d", 5001, "Asia 3GB 15 Days", 3072, 15, 12.99, 6.00, "region",
                List.of("JP", "KR", "TH", "SG", "MY", "ID", "PH", "VN"), "asia"),
            createProduct("asia_5gb_30d", 5002, "Asia 5GB 30 Days", 5120, 30, 19.99, 10.00, "region",
                List.of("JP", "KR", "TH", "SG", "MY", "ID", "PH", "VN"), "asia"),

            // Latin America Region
            createProduct("latam_5gb_30d", 6001, "Latin America 5GB 30 Days", 5120, 30, 17.99, 9.00, "region",
                List.of("MX", "BR", "AR", "CL", "CO", "PE"), "latam"),

            // Global Plans (111 countries)
            createProduct("global_3gb_15d", 7001, "Global 3GB 15 Days", 3072, 15, 29.99, 15.00, "global",
                GLOBAL_COUNTRIES, null),
            createProduct("global_5gb_30d", 7002, "Global 5GB 30 Days", 5120, 30, 44.99, 22.00, "global",
                GLOBAL_COUNTRIES, null),
            createProduct("global_10gb_30d", 7003, "Global 10GB 30 Days", 10240, 30, 69.99, 35.00, "global",
                GLOBAL_COUNTRIES, null)
        );
    }

    private MockProduct createProduct(String productId, Integer legacyId, String name, Integer dataQuotaMb,
                                      Integer validityDays, Double rrpUsd, Double wholesalePriceUsd,
                                      String packageType, List<String> countriesEnabled,
                                      String region) {
        MockProduct product = new MockProduct();

        // Generate UID
        String uid = "prod_" + UUID.randomUUID().toString().substring(0, 12);

        product.setUid(uid);
        product.setProductId(productId);
        product.setName(name);
        product.setDescription(String.format("%s data valid for %d days", formatDataSizeMB(dataQuotaMb), validityDays));

        // Maya API fields
        product.setDataQuotaMb(dataQuotaMb);
        product.setDataQuotaBytes((long) dataQuotaMb * 1024 * 1024);
        product.setValidityDays(validityDays);
        product.setCountriesEnabled(countriesEnabled);
        product.setWholesalePriceUsd(wholesalePriceUsd);
        product.setRrpUsd(rrpUsd);
        product.setRrpEur(rrpUsd * 0.92);
        product.setRrpGbp(rrpUsd * 0.80);
        product.setRrpCad(rrpUsd * 1.35);
        product.setRrpAud(rrpUsd * 1.50);
        product.setRrpJpy(rrpUsd * 145.0);
        product.setPolicyId("policy_" + packageType);
        product.setPolicyName(packageType.toUpperCase() + " Data Plan");

        // Legacy fields
        product.setDataGB((double) dataQuotaMb / 1024);
        product.setPrice(rrpUsd);
        product.setCurrency("USD");
        product.setWholesaleCost(wholesalePriceUsd);
        product.setCountries(countriesEnabled);

        product.setPackageType(packageType);
        product.setRegion(region);
        product.setActive(true);
        product.setTerms("Data valid for " + validityDays + " days from activation. No voice/SMS included.");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return product;
    }

    private String formatDataSizeMB(Integer dataMB) {
        if (dataMB >= 1024) {
            return String.format("%.0fGB", dataMB / 1024.0);
        } else {
            return String.format("%dMB", dataMB);
        }
    }
}
