package com.trend.cloudone.amaas;

import software.amazon.awssdk.services.s3.S3Client;

public class MockS3Client implements S3Client {
    @Override
    public String serviceName() {
        return "";
    }

    @Override
    public void close() {

    }
}
