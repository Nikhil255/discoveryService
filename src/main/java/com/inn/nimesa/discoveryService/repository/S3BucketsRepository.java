package com.inn.nimesa.discoveryService.repository;

import com.inn.nimesa.discoveryService.model.S3Buckets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface S3BucketsRepository extends JpaRepository<S3Buckets, Long> {
    S3Buckets findByBucketName(String bucketName);
}
