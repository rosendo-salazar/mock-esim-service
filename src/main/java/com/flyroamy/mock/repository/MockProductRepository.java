package com.flyroamy.mock.repository;

import com.flyroamy.mock.model.MockProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockProductRepository extends MongoRepository<MockProduct, String> {

    Optional<MockProduct> findByProductId(String productId);

    Optional<MockProduct> findByUid(String uid);

    boolean existsByProductId(String productId);

    boolean existsByUid(String uid);

    List<MockProduct> findByIsActiveTrue();

    Page<MockProduct> findByIsActiveTrue(Pageable pageable);

    @Query("{ 'countries': ?0, 'is_active': true }")
    List<MockProduct> findByCountryAndActive(String countryCode);

    @Query("{ 'countries': ?0, 'is_active': true }")
    Page<MockProduct> findByCountryAndActive(String countryCode, Pageable pageable);

    @Query("{ 'countries_enabled': ?0, 'is_active': true }")
    List<MockProduct> findByCountriesEnabledAndActive(String countryCode);

    @Query("{ 'countries_enabled': ?0, 'is_active': true }")
    Page<MockProduct> findByCountriesEnabledAndActive(String countryCode, Pageable pageable);

    @Query("{ 'region': ?0, 'is_active': true }")
    List<MockProduct> findByRegionAndActive(String region);

    @Query("{ 'region': ?0, 'is_active': true }")
    Page<MockProduct> findByRegionAndActive(String region, Pageable pageable);

    @Query("{ 'package_type': ?0, 'is_active': true }")
    List<MockProduct> findByPackageTypeAndActive(String packageType);

    @Query("{ 'package_type': ?0, 'is_active': true }")
    Page<MockProduct> findByPackageTypeAndActive(String packageType, Pageable pageable);

    void deleteByProductId(String productId);

    // Backward compatibility methods
    default Optional<MockProduct> findByBundleId(String bundleId) {
        return findByProductId(bundleId);
    }

    default boolean existsByBundleId(String bundleId) {
        return existsByProductId(bundleId);
    }

    default void deleteByBundleId(String bundleId) {
        deleteByProductId(bundleId);
    }
}
