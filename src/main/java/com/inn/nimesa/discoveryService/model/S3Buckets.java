package com.inn.nimesa.discoveryService.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Entity
@Data
public class S3Buckets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bucketName;

    @ElementCollection
    private List<String> fileNames;

    @OneToOne(mappedBy = "s3Buckets")
    private JobStatus jobStatus;

}
