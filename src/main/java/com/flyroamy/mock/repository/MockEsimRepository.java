package com.flyroamy.mock.repository;

import com.flyroamy.mock.model.MockEsim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockEsimRepository extends MongoRepository<MockEsim, String> {

    Optional<MockEsim> findByEsimId(String esimId);

    Optional<MockEsim> findByIccid(String iccid);

    Optional<MockEsim> findByUid(String uid);

    boolean existsByEsimId(String esimId);

    boolean existsByIccid(String iccid);

    List<MockEsim> findByStatus(String status);

    Page<MockEsim> findByStatus(String status, Pageable pageable);

    List<MockEsim> findByUserEmail(String userEmail);

    Page<MockEsim> findByUserEmail(String userEmail, Pageable pageable);

    @Query("{ 'metadata.userId': ?0 }")
    List<MockEsim> findByUserId(String userId);

    @Query("{ 'metadata.orderId': ?0 }")
    Optional<MockEsim> findByOrderId(String orderId);

    void deleteByEsimId(String esimId);

    long countByStatus(String status);
}
