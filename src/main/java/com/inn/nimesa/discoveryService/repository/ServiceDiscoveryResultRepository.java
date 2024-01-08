package com.inn.nimesa.discoveryService.repository;

import com.inn.nimesa.discoveryService.model.ServiceDiscoveryResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceDiscoveryResultRepository extends JpaRepository<ServiceDiscoveryResult, Long> {
}
