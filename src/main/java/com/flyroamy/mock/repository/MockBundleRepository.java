package com.flyroamy.mock.repository;

import com.flyroamy.mock.model.MockBundle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockBundleRepository extends MongoRepository<MockBundle, String> {

    Optional<MockBundle> findByBundleId(String bundleId);

    boolean existsByBundleId(String bundleId);

    List<MockBundle> findByIsActiveTrue();

    Page<MockBundle> findByIsActiveTrue(Pageable pageable);

    @Query("{ 'countries': ?0, 'is_active': true }")
    List<MockBundle> findByCountryAndActive(String countryCode);

    @Query("{ 'countries': ?0, 'is_active': true }")
    Page<MockBundle> findByCountryAndActive(String countryCode, Pageable pageable);

    @Query("{ 'region': ?0, 'is_active': true }")
    List<MockBundle> findByRegionAndActive(String region);

    @Query("{ 'region': ?0, 'is_active': true }")
    Page<MockBundle> findByRegionAndActive(String region, Pageable pageable);

    @Query("{ 'package_type': ?0, 'is_active': true }")
    List<MockBundle> findByPackageTypeAndActive(String packageType);

    @Query("{ 'package_type': ?0, 'is_active': true }")
    Page<MockBundle> findByPackageTypeAndActive(String packageType, Pageable pageable);

    void deleteByBundleId(String bundleId);
}
