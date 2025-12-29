package com.flyroamy.mock.service;

import com.flyroamy.mock.model.MockBundle;
import com.flyroamy.mock.repository.MockBundleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DataSeederService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeederService.class);

    private final MockBundleRepository bundleRepository;

    public DataSeederService(MockBundleRepository bundleRepository) {
        this.bundleRepository = bundleRepository;
    }

    @Override
    public void run(String... args) {
        if (bundleRepository.count() == 0) {
            logger.info("No bundles found, seeding initial data...");
            seedBundles();
        } else {
            logger.info("Bundles already exist, skipping seed. Count: {}", bundleRepository.count());
        }
    }

    public int seedBundles() {
        List<MockBundle> bundles = createSeedBundles();
        int created = 0;

        for (MockBundle bundle : bundles) {
            if (!bundleRepository.existsByBundleId(bundle.getBundleId())) {
                bundleRepository.save(bundle);
                created++;
                logger.info("Created bundle: {}", bundle.getName());
            }
        }

        logger.info("Seeded {} bundles", created);
        return created;
    }

    private List<MockBundle> createSeedBundles() {
        return List.of(
            // USA Plans
            createBundle("bundle_usa_1gb_7d", 1001, "USA 1GB 7 Days", 1.0, 7, 5.99,
                Map.of("USD", 5.99, "EUR", 5.49, "MXN", 109.00), 2.50, "country", List.of("us"), null, null),
            createBundle("bundle_usa_3gb_15d", 1002, "USA 3GB 15 Days", 3.0, 15, 9.99,
                Map.of("USD", 9.99, "EUR", 8.99, "MXN", 179.00), 4.50, "country", List.of("us"), null, null),
            createBundle("bundle_usa_5gb_30d", 1003, "USA 5GB 30 Days", 5.0, 30, 14.99,
                Map.of("USD", 14.99, "EUR", 13.49, "MXN", 269.00), 7.00, "country", List.of("us"), null, "Most Popular"),
            createBundle("bundle_usa_10gb_30d", 1004, "USA 10GB 30 Days", 10.0, 30, 24.99,
                Map.of("USD", 24.99, "EUR", 22.49, "MXN", 449.00), 12.00, "country", List.of("us"), null, "Best Value"),
            createBundle("bundle_usa_20gb_30d", 1005, "USA 20GB 30 Days", 20.0, 30, 39.99,
                Map.of("USD", 39.99, "EUR", 35.99, "MXN", 719.00), 20.00, "country", List.of("us"), null, null),

            // Mexico Plans
            createBundle("bundle_mexico_1gb_7d", 2001, "Mexico 1GB 7 Days", 1.0, 7, 4.99,
                Map.of("USD", 4.99, "EUR", 4.49, "MXN", 89.00), 2.00, "country", List.of("mx"), null, null),
            createBundle("bundle_mexico_5gb_30d", 2002, "Mexico 5GB 30 Days", 5.0, 30, 12.99,
                Map.of("USD", 12.99, "EUR", 11.69, "MXN", 229.00), 6.00, "country", List.of("mx"), null, "Most Popular"),
            createBundle("bundle_mexico_10gb_30d", 2003, "Mexico 10GB 30 Days", 10.0, 30, 19.99,
                Map.of("USD", 19.99, "EUR", 17.99, "MXN", 359.00), 10.00, "country", List.of("mx"), null, null),

            // Canada Plans
            createBundle("bundle_canada_3gb_15d", 3001, "Canada 3GB 15 Days", 3.0, 15, 11.99,
                Map.of("USD", 11.99, "EUR", 10.79, "CAD", 15.99), 5.50, "country", List.of("ca"), null, null),
            createBundle("bundle_canada_5gb_30d", 3002, "Canada 5GB 30 Days", 5.0, 30, 16.99,
                Map.of("USD", 16.99, "EUR", 15.29, "CAD", 22.99), 8.00, "country", List.of("ca"), null, "Most Popular"),

            // Europe Region
            createBundle("bundle_europe_3gb_15d", 4001, "Europe 3GB 15 Days", 3.0, 15, 14.99,
                Map.of("USD", 14.99, "EUR", 12.99, "GBP", 11.99), 7.00, "region",
                List.of("de", "fr", "it", "es", "nl", "be", "at", "pt", "ie", "gb"), "europe", null),
            createBundle("bundle_europe_5gb_30d", 4002, "Europe 5GB 30 Days", 5.0, 30, 24.99,
                Map.of("USD", 24.99, "EUR", 21.99, "GBP", 19.99), 12.00, "region",
                List.of("de", "fr", "it", "es", "nl", "be", "at", "pt", "ie", "gb"), "europe", "Most Popular"),
            createBundle("bundle_europe_10gb_30d", 4003, "Europe 10GB 30 Days", 10.0, 30, 39.99,
                Map.of("USD", 39.99, "EUR", 34.99, "GBP", 31.99), 20.00, "region",
                List.of("de", "fr", "it", "es", "nl", "be", "at", "pt", "ie", "gb"), "europe", "Best Value"),

            // Asia Region
            createBundle("bundle_asia_3gb_15d", 5001, "Asia 3GB 15 Days", 3.0, 15, 12.99,
                Map.of("USD", 12.99, "EUR", 11.69, "JPY", 1899.00), 6.00, "region",
                List.of("jp", "kr", "th", "sg", "my", "id", "ph", "vn"), "asia", null),
            createBundle("bundle_asia_5gb_30d", 5002, "Asia 5GB 30 Days", 5.0, 30, 19.99,
                Map.of("USD", 19.99, "EUR", 17.99, "JPY", 2899.00), 10.00, "region",
                List.of("jp", "kr", "th", "sg", "my", "id", "ph", "vn"), "asia", "Most Popular"),

            // Latin America Region
            createBundle("bundle_latam_5gb_30d", 6001, "Latin America 5GB 30 Days", 5.0, 30, 17.99,
                Map.of("USD", 17.99, "EUR", 16.19, "MXN", 323.00), 9.00, "region",
                List.of("mx", "br", "ar", "cl", "co", "pe"), "latam", "Most Popular"),

            // Global Plans
            createBundle("bundle_global_3gb_15d", 7001, "Global 3GB 15 Days", 3.0, 15, 29.99,
                Map.of("USD", 29.99, "EUR", 26.99, "GBP", 24.99), 15.00, "global",
                List.of("us", "ca", "mx", "gb", "de", "fr", "it", "es", "jp", "kr", "au"), null, null),
            createBundle("bundle_global_5gb_30d", 7002, "Global 5GB 30 Days", 5.0, 30, 44.99,
                Map.of("USD", 44.99, "EUR", 40.49, "GBP", 36.99), 22.00, "global",
                List.of("us", "ca", "mx", "gb", "de", "fr", "it", "es", "jp", "kr", "au"), null, "Most Popular"),
            createBundle("bundle_global_10gb_30d", 7003, "Global 10GB 30 Days", 10.0, 30, 69.99,
                Map.of("USD", 69.99, "EUR", 62.99, "GBP", 57.99), 35.00, "global",
                List.of("us", "ca", "mx", "gb", "de", "fr", "it", "es", "jp", "kr", "au"), null, "Best Value")
        );
    }

    private MockBundle createBundle(String bundleId, Integer productId, String name, Double dataGB,
                                    Integer validityDays, Double price, Map<String, Double> prices,
                                    Double wholesaleCost, String packageType, List<String> countries,
                                    String region, String badge) {
        MockBundle bundle = new MockBundle();
        bundle.setBundleId(bundleId);
        bundle.setProductId(productId);
        bundle.setName(name);
        bundle.setDescription(String.format("%s data valid for %d days", formatDataSize(dataGB), validityDays));
        bundle.setDataGB(dataGB);
        bundle.setValidityDays(validityDays);
        bundle.setPrice(price);
        bundle.setCurrency("USD");
        bundle.setPrices(prices);
        bundle.setWholesaleCost(wholesaleCost);
        bundle.setPackageType(packageType);
        bundle.setCountries(countries);
        bundle.setRegion(region);
        bundle.setActive(true);
        bundle.setBadge(badge);
        bundle.setTerms("Data valid for " + validityDays + " days from activation. No voice/SMS included.");
        bundle.setCreatedAt(LocalDateTime.now());
        bundle.setUpdatedAt(LocalDateTime.now());
        return bundle;
    }

    private String formatDataSize(Double dataGB) {
        if (dataGB >= 1) {
            return String.format("%.0fGB", dataGB);
        } else {
            return String.format("%.0fMB", dataGB * 1024);
        }
    }
}
