package com.inn.nimesa.discoveryService.service;
import com.inn.nimesa.discoveryService.model.JobStatus;
import com.inn.nimesa.discoveryService.model.S3Buckets;
import com.inn.nimesa.discoveryService.model.ServiceDiscoveryResult;
import com.inn.nimesa.discoveryService.model.Status;
import com.inn.nimesa.discoveryService.repository.JobStatusRepository;
import com.inn.nimesa.discoveryService.repository.S3BucketsRepository;
import com.inn.nimesa.discoveryService.repository.ServiceDiscoveryResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class DiscoveryService {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region.mumbai}")
    private String awsRegionMumbai;

    @Value("${aws.regions}")
    private List<String> awsRegions;

    @Autowired
    private ServiceDiscoveryResultRepository resultRepository;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private S3BucketsRepository s3BucketsRepository;

    public List<String> getDiscoveryResult(String service) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        if ("EC2".equals(service)) {
            return discoverEC2(awsCredentials);
        } else if ("S3".equals(service)) {
            return discoverS3(awsCredentials);
        }
        return new ArrayList<>();
    }

    public String discoverServices(List<String> services) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        String jobId = generateJobId();

        ServiceDiscoveryResult discoveryResult = new ServiceDiscoveryResult();
        JobStatus jobStatus = new JobStatus();
        jobStatus.setJobId(jobId);
        jobStatus.setStatus(Status.IN_PROGRESS);
        discoveryResult.setJobStatus(jobStatus);

        services.forEach(service -> {
            if ("EC2".equals(service)) {
                List<String> instanceIds = discoverEC2(awsCredentials);
                discoveryResult.setInstances(instanceIds);
            } else if ("S3".equals(service)) {
                List<String> bucketNames = discoverS3(awsCredentials);
                discoveryResult.setBuckets(bucketNames);
            }
        });
        jobStatus.setStatus(Status.SUCCESS);
        jobStatusRepository.save(jobStatus);
        resultRepository.save(discoveryResult);

        return jobId;
    }

    private String generateJobId() {
        return UUID.randomUUID().toString();
    }

    @Async
    public List<String> discoverEC2(AwsBasicCredentials awsCredentials) {

        Ec2Client ec2Client = Ec2Client.builder()
                .region(Region.of(awsRegionMumbai))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        DescribeInstancesResponse describeInstancesResponse = ec2Client.describeInstances();

        List<String> instanceIds = describeInstancesResponse.reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .map(Instance::instanceId)
                .collect(Collectors.toList());

        ec2Client.close();
        return instanceIds;
    }

    @Async
    public List<String> discoverS3(AwsBasicCredentials awsCredentials) {
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(awsRegionMumbai))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();

            ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
            List<String> bucketNames = listBucketsResponse.buckets().stream()
                    .map(Bucket::name).toList();
            s3Client.close();
            return bucketNames;
    }

    public String getJobResult(String jobId) {
        Optional<JobStatus> optionalJobStatus = jobStatusRepository.findByJobId(jobId);
        return optionalJobStatus.map(jobStatus -> jobStatus.getStatus().name()).orElse("Job not found");
    }

    public String getS3BucketObjects(String bucketName) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        String jobId = generateJobId();
        S3Buckets s3Buckets = new S3Buckets();
        JobStatus jobStatus = new JobStatus();
        jobStatus.setJobId(jobId);
        jobStatus.setStatus(Status.IN_PROGRESS);
        s3Buckets.setJobStatus(jobStatus);

        for (String awsRegion : awsRegions) {
            try {
                S3Client s3Client = S3Client.builder()
                        .region(Region.of(awsRegion))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                        .build();

                ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .build();

                ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
                List<S3Object> objects = listObjectsResponse.contents();

                List<String> fileNames = objects.stream()
                        .map(S3Object::key).toList();

                s3Buckets.setBucketName(bucketName);
                s3Buckets.setFileNames(fileNames);

                jobStatus.setStatus(Status.SUCCESS);
                jobStatusRepository.save(jobStatus);
                s3BucketsRepository.save(s3Buckets);

                return jobId;
            } catch (S3Exception e) {
                System.out.println("Bucket not found in region: " + awsRegion);
            }
        }
        jobStatus.setStatus(Status.FAILED);
        jobStatusRepository.save(jobStatus);
        return jobId;
    }

    public Long getS3BucketObjectCount(String bucketName) {
        S3Buckets s3Buckets = s3BucketsRepository.findByBucketName(bucketName);
        if (s3Buckets != null && s3Buckets.getFileNames() != null) {
            return (long) s3Buckets.getFileNames().size();
        }
        return 0L;
    }

    public List<String> getS3BucketObjectLike(String bucketName, String pattern) {
        Optional<S3Buckets> optionalS3Buckets = Optional.ofNullable(s3BucketsRepository.findByBucketName(bucketName));

        return optionalS3Buckets.map(s3Buckets ->
                s3Buckets.getFileNames().stream()
                        .filter(fileName -> fileName.contains(pattern))
                        .collect(Collectors.toList())
        ).orElseGet(List::of);
    }

}
