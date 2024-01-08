CREATE TABLE job_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE service_discovery_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service VARCHAR(255),
    job_status_id BIGINT,
    FOREIGN KEY (job_status_id) REFERENCES job_status(id)
);

CREATE TABLE s3_buckets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bucket_name VARCHAR(255),
    job_status_id BIGINT,
    FOREIGN KEY (job_status_id) REFERENCES job_status(id)
);
