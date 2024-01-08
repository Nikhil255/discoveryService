package com.inn.nimesa.discoveryService.controller;

// EC2DiscoveryController.java
import com.inn.nimesa.discoveryService.service.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    @Autowired
    private DiscoveryService discoveryService;

    @GetMapping("/discover-services")
    public List<String> discoverServices(@RequestParam("serviceName") String serviceName) {
        return discoveryService.getDiscoveryResult(serviceName);
    }

    @PostMapping("/discover-services")
    public ResponseEntity<String> discoverServices(@RequestBody List<String> services) {
        String jobId = discoveryService.discoverServices(services);
        return ResponseEntity.ok(jobId);
    }

    @GetMapping("/status/{jobId}")
    public String getJobStatus(@PathVariable String jobId) {
        return discoveryService.getJobResult(jobId);
    }

    @GetMapping("/get-s3-bucket-objects")
    public String getS3BucketObjects(@RequestParam("bucketName") String bucketName) {
        return discoveryService.getS3BucketObjects(bucketName);
    }

    @GetMapping("/get-s3-bucket-object-count/{bucketName}")
    public Long getS3BucketObjectCount(@PathVariable String bucketName) {
        return discoveryService.getS3BucketObjectCount(bucketName);
    }

    @GetMapping("/get-s3-bucket-object-like")
    public List<String> getS3BucketObjectLike(@RequestParam("bucketName") String bucketName,@RequestParam("pattern") String pattern) {
        return discoveryService.getS3BucketObjectLike(bucketName,pattern);
    }
}
