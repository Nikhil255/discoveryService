package com.inn.nimesa.discoveryService.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class JobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "jobStatus")
    private List<ServiceDiscoveryResult> discoveryResults;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "s3_buckets_id", referencedColumnName = "id")
    private S3Buckets s3Buckets;

}

