package com.inn.nimesa.discoveryService.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class ServiceDiscoveryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String service;

    private List<String> instances;

    private List<String> buckets;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private JobStatus jobStatus;

}

