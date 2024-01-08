package com.inn.nimesa.discoveryService.repository;

import com.inn.nimesa.discoveryService.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {

    Optional<JobStatus> findByJobId(String jobId);

}
